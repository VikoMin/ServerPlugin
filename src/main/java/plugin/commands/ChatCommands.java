package plugin.commands;

import arc.Events;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Timekeeper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import mindustry.maps.Map;
import mindustry.net.Administration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import plugin.commands.annotations.ChatCommand;
import plugin.commands.handlers.ChatListener;
import plugin.etc.Ranks;
import plugin.models.collections.PlayerData;
import useful.Bundle;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static arc.util.Strings.canParseInt;
import static mindustry.Vars.netServer;
import static mindustry.core.NetServer.voteCooldown;
import static plugin.ConfigJson.discordUrl;
import static plugin.Plugin.players;
import static plugin.Plugin.servers;
import static plugin.commands.Menus.achMenu;
import static plugin.commands.history.History.historyPlayers;
import static plugin.functions.Other.statsMenu;
import static plugin.utils.Utilities.*;

@SuppressWarnings("unused")
public class ChatCommands {
    public static AtomicInteger votes = new AtomicInteger(0);
    public static Seq<Player> votedPlayer = new Seq<>();
    public static boolean isVoting = false;
    private static final ObjectMap<String, Timekeeper> cooldowns = new ObjectMap<>();

    @ChatCommand(name = "announce", args = "<str text>", description = "calls an announce", requiredRank = Ranks.Rank.Moderator, minArgsCount = 1, isLastArgText = true)
    public void announce(Player player, List<String> args) {
        Call.announce(args.get(0));
    }

    @ChatCommand(name = "gameover", description = "Executes a gameover event", requiredRank = Ranks.Rank.Moderator)
    public void gameover(Player player, List<String> args) {
        Events.fire(new EventType.GameOverEvent(Team.derelict));
    }

    @ChatCommand(name = "players", description = "Lists all players on the server")
    public void players(Player player, List<String> args) {
        StringBuilder list = new StringBuilder();
        for (Player plr : Groups.player) {
            plugin.models.wrappers.PlayerData data = new plugin.models.wrappers.PlayerData(plr);
            if (data.isExist()) {
                list.append(plr.name()).append("; [white]ID: ").append(data.getId()).append("\n");
            }
        }
        player.sendMessage(String.valueOf(list));
    }

    @ChatCommand(name = "js", args = "<str code>", description = "Execute JS code", requiredRank = Ranks.Rank.JS, minArgsCount = 1, isLastArgText = true)
    public void javascript(Player player, List<String> args) {
        runJs(args.get(0), resp -> {
            if (!resp.isEmpty()) player.sendMessage(resp);
        });
    }

    @ChatCommand(name = "maps", args = "[int page]", description = "List all maps", maxArgsCount = 1)
    public void maps(Player player, List<String> args) {
        StringBuilder list = new StringBuilder();
        int page;
        if (args.isEmpty()) {
            page = 0;
        } else {
            if (!canParseInt(args.get(0))) {
                player.sendMessage("[red]Page must be number!");
                return;
            }
            page = Integer.parseInt(args.get(0));
        }
        int mapsPerPage = 10;
        Seq<Map> maps = getMaps();
        maps.list().stream().skip(page * 10L).limit(mapsPerPage + (page * 10L)).forEach(
                map -> list.append(map.name()).append("[white], by ").append(map.author()).append("\n")
        );
        if (!String.valueOf(list).contains("by")) {
            player.sendMessage("[red]No maps detected!");
            return;
        }
        player.sendMessage(String.valueOf(list));
    }

    @ChatCommand(name = "rtv", args = "[str map_name]", description = "Rock the vote to change map!", maxArgsCount = 1, isLastArgText = true)
    public void rtv(Player player, List<String> args) {
        final int[] votesRequired = new int[1];
        AtomicInteger time = new AtomicInteger(60);
        votesRequired[0] = (int) Math.ceil((double) Groups.player.size() / 2);
        Timer timer = new Timer();
        if (isVoting) {
            player.sendMessage("Vote is already running!");
            return;
        }
        Map choosedMap = Vars.maps.customMaps().find(map -> Strings.stripColors(map.name().toLowerCase()).contains(args.get(0).toLowerCase()));
        if (choosedMap == null) {
            player.sendMessage("Could not find that map!");
            return;
        }
        Call.sendMessage(player.name() + "[#e7e7e7] Started vote for map " + choosedMap.plainName() + "[#e7e7e7] -> " + votes.get() + "/" + votesRequired[0] + ", y/n to vote");
        isVoting = true;
        timer.schedule((new TimerTask() {
            @Override
            public void run() {
                time.getAndAdd(-1);
                votesRequired[0] = (int) Math.ceil((double) Groups.player.size() / 2);
                if (votes.get() >= votesRequired[0]) {
                    voteSuccess(choosedMap);
                    isVoting = false;
                    timer.cancel();
                }
                if (time.get() < 0) {
                    voteCanceled();
                    isVoting = false;
                    timer.cancel();
                }
            }
        }), 0, 1000);
    }

    @ChatCommand(name = "discord", description = "Link to our discord!")
    public void discord(Player player, List<String> args) {
        Call.openURI(player.con, discordUrl);
    }

    @ChatCommand(name = "stats", args = "[str name]", description = "Get stats of player or yourself", maxArgsCount = 1)
    public void stats(Player player, List<String> args) {
        if (args.isEmpty()) {
            statsMenu(player, player);
            return;
        }
        Player plr = findPlayerByName(args.get(0));
        if (plr == null) {
            player.sendMessage("Could not find that player!");
            return;
        }
        statsMenu(player, plr);
    }

    @ChatCommand(name = "history", description = "Enables/Disables history mode")
    public void history(Player player, List<String> args) {
        if (historyPlayers.contains(player.uuid())) {
            historyPlayers.remove(player.uuid());
            player.sendMessage("[red]History has been disabled!");
            Call.hideHudText(player.con());
            return;
        }
        historyPlayers.add(player.uuid());
        player.sendMessage("[green]History has been enabled!");
    }

    @ChatCommand(name = "joinmessage", args = "<str message>", description = "Makes custom join message! @ -> your name. Make sure this message wont break any rule!", minArgsCount = 1)
    public void joinMessage(Player player, List<String> args) {
        plugin.models.wrappers.PlayerData data = new plugin.models.wrappers.PlayerData(player);
        if (args.get(0).length() >= 45) {
            player.sendMessage("Too much symbols! Limit is 45");
        } else {
            data.setJoinMessage(args.get(0));
            player.sendMessage("[green]Changed your join message!");
        }
    }

    @ChatCommand(name = "leaderboard", description = "Shows leaderboard")
    public void leaderboard(Player player, List<String> args) {
        StringBuilder list = new StringBuilder();
        list.append("[orange]Playtime leaderboard: \n");
        FindIterable<PlayerData> sort = players.find().sort(new BasicDBObject("playtime", -1)).limit(10);
        for (PlayerData data : sort) {
            int playtime = data.playtime;
            list.append(data.rawName).append("[white]: ").append(Bundle.formatDuration(Duration.ofMinutes(playtime))).append("\n");
        }
        player.sendMessage(list.toString());
    }

    @ChatCommand(name = "achievements", description = "Views your achievements")
    public void achievements(Player player, List<String> args){
        achMenu(player);
    }

    @ChatCommand(name = "serverhop", args = "<str server_name>", description = "Hops to server", minArgsCount = 1)
    public void serverHop(Player player, List<String> args){
        JSONArray array = (JSONArray) servers.get("servers");
        Seq<Server> servers = new Seq<>();
        for (Object object : array) {
            JSONObject jsonObject = (JSONObject) object;
            Server serv = new Server((String) jsonObject.get("servername"), (String) jsonObject.get("ip"), (Long) jsonObject.get("port"));
            servers.add(serv);
        }
        if (servers.contains(server -> server.name.contains(args.get(0)))) {
            Server serv = servers.find(server -> server.name.contains(args.get(0)));
            Call.connect(player.con, serv.ip, Math.toIntExact(serv.port));
        }
    }

    @ChatCommand(name = "servers", description = "Lists all servers")
    public void servers(Player player, List<String> args){
        JSONArray array = (JSONArray) servers.get("servers");
        StringBuilder list = new StringBuilder();
        list.append("[yellow]SERVER LIST:\n\n[white]");
        for (Object object : array) {
            JSONObject jsonObject = (JSONObject) object;
            list.append(jsonObject.get("servername")).append(": ").append(jsonObject.get("ip")).append(":").append(jsonObject.get("port")).append("\n");
        }
        player.sendMessage(list.toString());
    }
    @ChatCommand(name = "help", description = "List all commands", args = "[int page]", maxArgsCount = 1)
    public void help(Player player, List<String> args) {
        int page = 1;
        if (!args.isEmpty() && Strings.canParseInt(args.get(0))) page = Integer.parseInt(args.get(0));
        int commandsPerPage = 6;
        int pages = Mathf.ceil((float) ChatListener.commands.size / commandsPerPage);
        page--;
        if(page >= pages || page < 0){
            player.sendMessage("[scarlet]'page' must be a number between[orange] 1[] and[orange] " + pages + "[scarlet].");
            return;
        }
        StringBuilder result = new StringBuilder();
        result.append(Strings.format("[orange]-- Commands Page[lightgray] @[gray]/[lightgray]@[orange] --\n\n", (page + 1), pages));

        for (int i = commandsPerPage * page; i < Math.min(commandsPerPage * (page + 1), ChatListener.commands.size); i++){
            ChatCommand command = ChatListener.commands.get(i);
            result.append("[orange] /").append(command.name()).append("[white] ").append(command.args()).append("[lightgray] - ").append(command.description()).append("\n");
        }
        player.sendMessage(result.toString());
    }
    @ChatCommand(name = "t", description = "Send a message only to your teammates.", args = "<str message...>", minArgsCount = 1, isLastArgText = true)
    public void t(Player player, List<String> args) {
        String message = Vars.netServer.admins.filterMessage(player, Strings.join(" ", args));
        if(message != null){
            String raw = "[#" + player.team().color.toString() + "]<T> " + Vars.netServer.chatFormatter.format(player, message);
            Groups.player.each(p -> p.team() == player.team(), o -> o.sendMessage(raw, player, message));
        }
    }
    @ChatCommand(name = "a", description = "Send a message only to admins.", args = "<str message...>", minArgsCount = 1, isLastArgText = true, requiredRank = Ranks.Rank.Moderator)
    public void a(Player player, List<String> args) {
        String raw = "[#" + Pal.adminChat.toString() + "]<A> " + Vars.netServer.chatFormatter.format(player, Strings.join(" ", args));
        Groups.player.each(Player::admin, a -> a.sendMessage(raw, player, Strings.join(" ", args)));
    }
    @ChatCommand(name = "sync", description = "Re-synchronize world state.")
    public void sync(Player player, List<String> args) {
        if(player.isLocal()){
            player.sendMessage("[scarlet]Re-synchronizing as the host is pointless.");
        } else {
            if(Time.timeSinceMillis(player.getInfo().lastSyncTime) < 1000 * 5){
                player.sendMessage("[scarlet]You may only /sync every 5 seconds.");
                return;
            }
            player.getInfo().lastSyncTime = Time.millis();
            Call.worldDataBegin(player.con);
            netServer.sendWorldData(player);
        }
    }
    @ChatCommand(name = "votekick", description = "Vote to kick a player with a valid reason.", args = "[player] [reason...]", maxArgsCount = 2, isLastArgText = true)
    public void votekick(Player player, List<String> args) {
        var session = VoteSession.getInstance();
        if (!Administration.Config.enableVotekick.bool()) {
            player.sendMessage("[scarlet]Vote-kick is disabled on this server.");
        } else if (Groups.player.size() < 3) {
            player.sendMessage("[scarlet]At least 3 players are needed to start a votekick.");
        } else if (player.isLocal()) {
            player.sendMessage("[scarlet]Just kick them yourself if you're the host.");
        } else if (session != null && session.isAlive) {
            player.sendMessage("[scarlet]A vote is already in progress.");
        } else {
            if (args.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append("[orange]Players to kick: \n");
                Groups.player.each((p) -> !p.admin && p.con != null && p != player, (p) -> {
                    builder.append("[lightgray] ").append(p.name).append("[accent] (#").append(p.id()).append(")\n");
                });
                player.sendMessage(builder.toString());
            } else if (args.size() == 1) {
                player.sendMessage("[orange]You need a valid reason to kick the player. Add a reason after the player name.");
            } else {
                Player found;
                if (args.get(0).length() > 1 && args.get(0).startsWith("#") && Strings.canParseInt(args.get(0).substring(1))) {
                    int id = Strings.parseInt(args.get(0).substring(1));
                    found = Groups.player.find((p) -> p.id() == id);
                } else {
                    found = Groups.player.find((p) -> p.name.equalsIgnoreCase(args.get(0)));
                }

                if (found != null) {
                    if (found == player) {
                        player.sendMessage("[scarlet]You can't vote to kick yourself.");
                    } else if (found.admin) {
                        player.sendMessage("[scarlet]Did you really expect to be able to kick an admin?");
                    } else if (found.isLocal()) {
                        player.sendMessage("[scarlet]Local players cannot be kicked.");
                    } else if (found.team() != player.team()) {
                        player.sendMessage("[scarlet]Only players on your team can be kicked.");
                    } else {
                        Timekeeper vtime = cooldowns.get(player.uuid(), () -> new Timekeeper(voteCooldown));
                        if (!vtime.get()) {
                            player.sendMessage("[scarlet]You must wait " + voteCooldown / 60 + " minutes between votekicks.");
                            return;
                        }

                        session = VoteSession.newSession(found, player, 2 + (Groups.player.size() > 4 ? 1 : 0), args.get(1));
                        session.vote(player, 1);
                        Call.sendMessage(Strings.format("[lightgray]Reason:[orange] @[lightgray].", args.get(1)));
                        vtime.reset();
                    }
                } else {
                    player.sendMessage("[scarlet]No player [orange]'" + args.get(0) + "'[scarlet] found.");
                }
            }

        }
    }
    @ChatCommand(name = "vote", description = "Vote to kick the current player. Admin can cancel the voting with 'c'.", args = "<y/n/c>", minArgsCount = 1)
    public void vote(Player player, List<String> args) {
        VoteSession session = VoteSession.getInstance();
        if (session == null || !session.isAlive) {
            player.sendMessage("[scarlet]Nobody is being voted on.");
            return;
        }
        switch (args.get(0).charAt(0)) {
            case 'y', 'Y' -> {
                session.vote(player, 1);
            }
            case 'n', 'N' -> {
                session.vote(player, -1);
            }
            case 'c', 'C' -> {
                if (!player.admin) {
                    player.sendMessage("[#ff]You must be admin to cancel votes.");
                    return;
                }
                Call.sendMessage(Strings.format("[lightgray]Vote canceled by admin[orange] @[lightgray].", player.coloredName()));
                session.voteTask.cancel();
                session.isAlive = false;
            }
            default -> {
                player.sendMessage("[#ff]Type y, n, or c");
            }
        }
    }
}
