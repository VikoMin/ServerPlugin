package plugin;

import arc.ApplicationListener;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Time;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.SendChatMessageCallPacket;
import mindustry.net.Packets;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import plugin.commands.VoteSession;
import plugin.commands.handlers.ChatListener;
import plugin.configs.ConfigJson;
import plugin.database.collections.UsidBan;
import plugin.discord.Bot;
import plugin.funcs.AntiVpn;
import plugin.database.collections.PlayerData;
import plugin.models.Ranks;
import plugin.models.Sessions;
import useful.Bundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static mindustry.Vars.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static plugin.configs.ConfigJson.discordUrl;
import static plugin.configs.ServersConfig.makeServersConfig;
import static plugin.menus.BanMenu.loadBanMenu;
import static plugin.commands.ConsoleCommands.loadServerCommands;
import static plugin.commands.ChatCommands.*;
import static plugin.commands.history.History.historyPlayers;
import static plugin.commands.history.History.loadHistory;
import static plugin.funcs.AntiVpn.loadAntiVPN;
import static plugin.funcs.Other.kickIfBanned;
import static plugin.funcs.Other.welcomeMenu;


public class Plugin extends mindustry.mod.Plugin implements ApplicationListener {
    public static MongoClient mongoClient;
    public static MongoDatabase db;
    public static MongoCollection<PlayerData> players;
    public static MongoCollection<UsidBan> usidBans;
    public static JSONObject servers;
    public static Sessions sessions;

    static {
        try {
            servers = makeServersConfig();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Plugin() throws IOException, ParseException {
        ConfigJson.read();
        Bot.load();
        sessions = new Sessions();
        ConnectionString string = new ConnectionString(ConfigJson.mongodbUrl);
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        mongoClient = MongoClients.create(string);
        db = mongoClient.getDatabase("mindustry").withCodecRegistry(pojoCodecRegistry);
        players = db.getCollection("players", PlayerData.class);
        usidBans = db.getCollection("usidbans", UsidBan.class);
        File dir = new File(Vars.tmpDirectory.absolutePath());
        if (!dir.exists())
            dir.mkdir();
        File sessionsCSV = new File("sessions.csv");
        if (!sessionsCSV.exists()){
            sessionsCSV.createNewFile();
            FileWriter csv = new FileWriter("sessions.csv");
            csv.append("id;connectionTime;disconnectionTime;messages;built;destroyed\n");
            csv.close();
        }
    }

    //  starts once plugin is started
    public void init() {
        loadAntiVPN();
        loadBanMenu();
        loadHistory();
        Log.info("Plugin started!");
        Bundle.load(Plugin.class);

        Events.on(EventType.PlayerConnect.class, event -> {
                  kickIfBanned(event.player);
        });

        Events.on(EventType.PlayerJoin.class, event -> {
            Player plr = event.player;
            plr.name = plr.name.replace("@", "");
            if (!plr.admin) welcomeMenu(plr);
            plugin.database.wrappers.PlayerData data = new plugin.database.wrappers.PlayerData(event.player);
            sessions.createSession(player);
            if (data.getRank().equal(Ranks.Rank.Moderator)) {
              plr.admin(data.getAdminUsids().contains(plr.usid()));
            }
            String joinMessage = data.getJoinMessage().trim();
            Call.sendMessage(joinMessage.replace("@", plr.name()) + " [grey][" + data.getId() + "]");
            Log.info(plr.plainName() + " joined! " + "[" + data.getId() + "]");
        });

        net.handleServer(Packets.Connect.class, (con, connect) -> {
            Events.fire(new EventType.ConnectionEvent(con));

            if (netServer.admins.isIPBanned(connect.addressTCP) || netServer.admins.isSubnetBanned(connect.addressTCP)) {
                con.kick(Packets.KickReason.banned);
            }
            kickIfBanned(con);
            if (AntiVpn.checkAddress(connect.addressTCP))
                con.kick("[orange]You are suspected in using VPN or being a bot! Please, if its not true, report that incident on our discord: " + discordUrl);
        });

        net.handleServer(SendChatMessageCallPacket.class, (con, packet) -> {
            Player player = con.player;
            if (player == null) return;
            if (packet.message == null) return;
            if (player.con.hasConnected && player.isAdded()) {
                String message = packet.message;
                if (message.length() > Vars.maxTextLength) {
                    player.sendMessage("Message too long");
                    return;
                }
                Events.fire(new EventType.PlayerChatEvent(player, message));
                Log.info("[@]: @", player.plainName(), message);
                if (message.startsWith("/")) {
                    ChatListener.handleCommand(player, message.substring(1));
                } else {
                    message = Vars.netServer.admins.filterMessage(player, message.replace("\n", ""));
                    if (message == null) return;
                    Call.sendMessage("[coral][\f" + player.coloredName() + "\f[coral]][white]: " + message, message, player);
                }
            }
        });

        Events.on(EventType.PlayerChatEvent.class, event -> {
            sessions.getSession(event.player).increaseMessages();
            if (isVoting) {
                int votesRequired = (int) Math.ceil((double) Groups.player.size() / 2);
                switch (event.message) {
                    case "y" -> {
                        if (votedPlayer.contains(event.player)) {
                            event.player.sendMessage("You already voted!");
                            return;
                        }
                        votes.getAndAdd(1);
                        votedPlayer.add(event.player);
                        Call.sendMessage(event.player.plainName() + " Voted: " + votes.get() + "/" + votesRequired);
                    }
                    case "n" -> {
                        if (votedPlayer.contains(event.player)) {
                            event.player.sendMessage("You already voted!");
                            return;
                        }
                        votes.getAndAdd(-1);
                        votedPlayer.add(event.player);
                        Call.sendMessage(event.player.plainName() + " Voted: " + votes.get() + "/" + votesRequired);
                    }
                }
            }
        });

        Events.on(EventType.BlockBuildEndEvent.class, event -> {
            if (event.unit.isPlayer())
                sessions.getSession(event.unit.getPlayer()).increaseBlocks(event.breaking);
        });

        Events.on(EventType.PlayerLeave.class, event -> {
            Player player = event.player;
            sessions.closeSession(player);
            historyPlayers.remove(player.uuid());
            plugin.database.wrappers.PlayerData data = new plugin.database.wrappers.PlayerData(player);
            Call.sendMessage(player.name() + "[white] left " + "[grey][" + data.getId() + "]");
            Log.info(player.plainName() + " left " + "[" + data.getId() + "]");
            VoteSession session = VoteSession.getInstance();
            if ((session != null && session.isAlive) && session.target.uuid().equals(event.player.uuid())) {
                session.pass();
                data.setLastBanTime(Time.millis() + (6 * 3600000)); // 6hour ban
            }
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        loadServerCommands(handler);
    }
}
