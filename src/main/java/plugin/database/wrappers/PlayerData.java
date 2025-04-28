package plugin.database.wrappers;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import plugin.models.Ranks;

import java.util.ArrayList;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static plugin.Plugin.players;

public class PlayerData {
    private final plugin.database.collections.PlayerData collection;

    public static ArrayList<PlayerData> findByRank(String rankName) {
        ArrayList<PlayerData> output = new ArrayList<>();
        Ranks.Rank rank = Ranks.getRank(rankName);
        try (MongoCursor<plugin.database.collections.PlayerData> cursor = players.find(Filters.eq("rank", rank.ordinal())).limit(25).iterator()) {
            while (cursor.hasNext())
                output.add(new PlayerData(cursor.next()));
        }
        return output;
    }

    public static long getIdBySnowFlake(long snowflake) {
        PlayerData data = new PlayerData(players.find(eq("discordId", snowflake)).first());
        return (data.isExist()) ? data.getId() : snowflake;
    }

    public PlayerData(plugin.database.collections.PlayerData collection){
        this.collection = collection;
    }
    public PlayerData(int id) {
        collection = players.find(eq("_id", id)).first();
    }

    public PlayerData(String uuid) {
        collection = players.find(eq("uuid", uuid)).first();
    }
    public PlayerData(NetConnection player){
        collection = players.find(eq("uuid", player.uuid)).first();
    }
    public PlayerData(Player player) {
        collection = Optional.ofNullable(players.find(eq("uuid", player.uuid())).first()).orElse(
                new plugin.database.collections.PlayerData(getNextID(), player.uuid()));
        if (!collection.names.contains(player.plainName())) collection.names.add(player.plainName());
        collection.rawName = player.name();
        if (!collection.ips.contains(player.con.address)) collection.ips.add(player.con.address);
        commit();
    }

    public boolean isExist() {
        return collection != null;
    }

    public void commit() {
        players.replaceOne(eq("_id", collection.id), collection, new ReplaceOptions().upsert(true));
    }

    public static int getNextID() {
        plugin.database.collections.PlayerData data = players.find().sort(new BasicDBObject("_id", -1)).first();
        return (data == null) ? 0 : data.id + 1;
    }

    public Player getPlayer(){
        return Groups.player.find(t-> t.uuid().equals(getUuid()));
    }

    //setters
    public void setLastBanTime(long time) {
        collection.lastBan = time;
        commit();
    }

    public void setRank(Ranks.Rank rank) {
        collection.rank = rank.ordinal();
        commit();
    }
    public void setDiscordId(long id){
        collection.discordId = id;
        commit();
    }

    public void setRank(String rank) {
        collection.rank = Ranks.getRank(rank).ordinal();
        commit();
    }

    public void setVip(boolean isVip) {
        collection.isVip = isVip;
        commit();
    }
    public void setJoinMessage(String message){
        collection.joinMessage = message;
        commit();
    }
    public void addUsid(String usid) {
        if (!collection.adminUsids.contains(usid)) {
            collection.adminUsids.add(usid);
            commit();
        }
    }
    public void removeUsids() {
        collection.adminUsids.clear();
        commit();
    }
    //mutators
    public void increasePlaytime(int value) {
        collection.playtime += value;
        commit();
    }

    //getters
    public int getId() {
        if (isExist())
            return collection.id;
        return -1;
    }

    public String getUuid() {
        if (isExist())
            return collection.uuid;
        return "";
    }

    public ArrayList<String> getNames() {
        if (isExist())
            return collection.names;
        return new ArrayList<>();
    }

    public String getLastName() {
        if (isExist())
            return collection.names.get(collection.names.size() - 1);
        return "";
    }

    public Ranks.Rank getRank() {
        if (isExist())
            return Ranks.getRank(collection.rank);
        return Ranks.Rank.None;
    }

    public String getJoinMessage() {
        if (isExist())
            return collection.joinMessage;
        return "@ joined!";
    }

    public ArrayList<String> getIPs() {
        if (isExist())
            return collection.ips;
        return new ArrayList<>();
    }

    public long getLastBanTime() {
        if (isExist())
            return collection.lastBan;
        return -1;
    }

    public long getDiscordId() {
        if (isExist())
            return collection.discordId;
        return 0;
    }

    public long getPlaytime() {
        if (isExist())
            return collection.playtime;
        return 0;
    }
    public ArrayList<String> getAdminUsids() {
        if (isExist()) return collection.adminUsids;
        return new ArrayList<>();
    }
}
