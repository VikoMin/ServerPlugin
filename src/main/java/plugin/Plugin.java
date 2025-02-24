package plugin;

import arc.ApplicationListener;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
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
import mindustry.net.Packets;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import plugin.discord.Bot;
import plugin.etc.AntiVpn;
import plugin.models.PlayerData;
import useful.Bundle;

import java.io.File;
import java.io.IOException;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static mindustry.Vars.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static plugin.ConfigJson.discordUrl;
import static plugin.ServersConfig.makeServersConfig;
import static plugin.commands.BanMenu.loadBanMenu;
import static plugin.commands.ConsoleCommands.loadServerCommands;
import static plugin.commands.MainCommands.*;
import static plugin.commands.history.History.historyPlayers;
import static plugin.commands.history.History.loadHistory;
import static plugin.etc.AntiVpn.loadAntiVPN;
import static plugin.functions.MongoDB.*;
import static plugin.functions.Other.kickIfBanned;
import static plugin.functions.Other.welcomeMenu;
import static plugin.utils.FindDocument.getPlayerData;


public class Plugin extends mindustry.mod.Plugin implements ApplicationListener{
    public static MongoClient mongoClient;
    public static MongoDatabase db;
    public static MongoCollection<PlayerData> newCollection;
    public static JSONObject servers;

    static {
        try {
            servers = makeServersConfig();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    // loads bot and other shit
    public Plugin() throws IOException, ParseException {
        ConfigJson.read();
        Bot.load();
        ConnectionString string = new ConnectionString(ConfigJson.mongodbUrl);
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        mongoClient = MongoClients.create(string);
        db = mongoClient.getDatabase("mindustry").withCodecRegistry(pojoCodecRegistry);
        newCollection = db.getCollection("newplayers", PlayerData.class);
        File dir = new File(Vars.tmpDirectory.absolutePath());
        if (!dir.exists()){
            dir.mkdir();
        }
    }

    //  starts once plugin is started
    public void init() {
        loadAntiVPN();
        loadBanMenu();
        loadHistory();
        Log.info("Plugin started!");
        Bundle.load(Plugin.class);
        Events.on(EventType.PlayerJoin.class, event -> {
            Player plr = event.player;
            if(!plr.admin) welcomeMenu(plr);
            PlayerData data = findPlayerDataOrCreate(event.player);
            fillData(data, event.player);
            kickIfBanned(event.player);
            String joinMessage = data.joinMessage;
            if (joinMessage.endsWith(" ")){
                joinMessage = joinMessage.substring(0, joinMessage.length()-1);
            }
            Call.sendMessage(Strings.format(joinMessage + " [grey][" + data.id + "]", plr.name()));
            Log.info(plr.plainName() + " joined! " + "[" + data.id + "]");
        });
        MongoDbPlaytimeTimer();
        net.handleServer(Packets.Connect.class, (con, connect) -> {
            Events.fire(new EventType.ConnectionEvent(con));
            MongoDbPlayerIpCheck(con);

            if (netServer.admins.isIPBanned(connect.addressTCP) || netServer.admins.isSubnetBanned(connect.addressTCP)){
                con.kick(Packets.KickReason.banned);
            }
            kickIfBanned(con);
            if (AntiVpn.checkAddress(connect.addressTCP))
                con.kick("[orange]You are suspected in using VPN or being a bot! Please, if its not true, report that incident on our discord: " + discordUrl);
        });
        Events.on(EventType.PlayerChatEvent.class, event ->{
            if (isVoting){
                int votesRequired = (int) Math.ceil((double) Groups.player.size()/2);
                switch (event.message){
                    case "y" ->{
                        if (votedPlayer.contains(event.player)){
                            event.player.sendMessage("You already voted!");
                            return;
                        }
                        votes.getAndAdd(1);
                        votedPlayer.add(event.player);
                        Call.sendMessage(event.player.plainName() +" Voted: " + votes.get() +"/"+ votesRequired);
                    }
                    case "n" ->{
                        if (votedPlayer.contains(event.player)){
                            event.player.sendMessage("You already voted!");
                            return;
                        }
                        votes.getAndAdd(-1);
                        votedPlayer.add(event.player);
                        Call.sendMessage(event.player.plainName() +" Voted: " + votes.get() +"/"+ votesRequired);
                    }
                }
            }
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            Player player = event.player;
            historyPlayers.remove(player.uuid());
            PlayerData data = getPlayerData(player.uuid());
            if (data == null) return;
            Call.sendMessage(player.name() + "[white] left " + "[grey][" + data.id + "]");
            Log.info(player.plainName() + " left " + "[" + data.id + "]");
        });
    }


    @Override
    public void registerClientCommands(CommandHandler handler){
        loadClientCommands(handler);
    }
    @Override
    public void registerServerCommands(CommandHandler handler){
        loadServerCommands(handler);
    }
}
