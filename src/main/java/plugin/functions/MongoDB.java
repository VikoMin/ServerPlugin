package plugin.functions;

import arc.struct.ObjectSet;
import arc.util.Log;
import arc.util.Timer;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration;
import mindustry.net.NetConnection;
import plugin.models.PlayerData;

import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static plugin.Plugin.newCollection;
import static plugin.utils.FindDocument.getPlayerData;
import static plugin.utils.FindDocument.getPlayerDataByIP;

public class MongoDB {
    public static PlayerData findPlayerDataOrCreate(Player eventPlayer){
        return Optional.ofNullable(newCollection.find(eq("uuid", eventPlayer.uuid())).first()).orElse(
                new PlayerData(eventPlayer.uuid(), getNextID())
        );
    }
    public static void fillData(PlayerData data, Player plr){
        data.name = plr.plainName();
        data.rawName = plr.name();
        data.ip = plr.con.address;
        data.uuid = plr.uuid();
        newCollection.replaceOne(eq("_id", data.id), data, new ReplaceOptions().upsert(true));
    }
    public static void MongoDbPlayerIpCheck(NetConnection player){
        PlayerData data = getPlayerDataByIP(player.address);
        if (data == null) {
            return;
        }
        data.ip = player.address;
        MongoDbUpdate(data);
    }
    public static <T> void MongoDbUpdate(PlayerData data){
        newCollection.replaceOne(eq("_id", data.id), data, new ReplaceOptions().upsert(true));
    }
    public static void MongoDbPlaytimeTimer(){
            Timer.schedule(() -> {
                for (Player player : Groups.player){
                    PlayerData data = getPlayerData(player.uuid());
                    if (data == null) return;
                    data.playtime += 1;
                    MongoDbUpdate(data);
                }
            try (MongoCursor<PlayerData> cursor = newCollection.find(Filters.gte("playtime", 2250)).iterator()) {
                while (cursor.hasNext()) {
                    PlayerData csr = cursor.next();
                    String activeAch = "[lime]Hyper[green]active";
                    if (!csr.achievements.contains(activeAch)) {
                        Log.debug(csr.name);
                        csr.achievements.add(activeAch);
                        MongoDbUpdate(csr);
                    }
                }
            }
            try (MongoCursor<PlayerData> cursor = newCollection.find(Filters.lte("_id", 150)).iterator()) {
                while (cursor.hasNext()) {
                    PlayerData csr = cursor.next();
                    String activeAch = "[orange]Vete[yellow]ran";
                    if (!csr.achievements.contains(activeAch)) {
                        Log.debug(csr.name);
                        csr.achievements.add(activeAch);
                        MongoDbUpdate(csr);
                    }
                }
            }
        }, 0, 60);
    }
    public static void MongoDbCheck(){
        try (MongoCursor<PlayerData> cursor = newCollection.find().iterator()) {
            while (cursor.hasNext()) {
                PlayerData csr = cursor.next();
                ObjectSet<Administration.PlayerInfo> ip = Vars.netServer.admins.findByName(csr.uuid);
                /*if (ip.size != 0) {
                    appendIfNull(csr, "joinmessage", "@ [white]joined");
                }*/
            }
        }
    }
/*    public static void appendIfNull(Document user, String key, Object defaultValue){
        if (user.get(key) == null){
            MongoDbUpdate(user, Updates.set(key, defaultValue));
        }
    }*/
    public static int getNextID(){
        PlayerData data = newCollection.find().sort(new BasicDBObject("_id", -1)).first();
        if (data == null){
            return 0;
        }
        return data.id + 1;
    }
}
