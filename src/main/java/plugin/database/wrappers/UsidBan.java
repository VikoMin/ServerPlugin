package plugin.database.wrappers;

import arc.util.Time;
import com.mongodb.client.model.ReplaceOptions;
import mindustry.gen.Player;

import static com.mongodb.client.model.Filters.eq;
import static plugin.Plugin.usidBans;

public class UsidBan {
    private final plugin.database.collections.UsidBan collection;

    public UsidBan(String usid) {
        collection = usidBans.find(eq("usid", usid)).first();
    }

    public UsidBan(Player player) {
        this(player.usid());
    }

    private UsidBan() {
        collection = new plugin.database.collections.UsidBan();
    }

    private UsidBan(plugin.database.collections.UsidBan usidBan) {
        collection = usidBan;
    }

    public static UsidBan findByUuid(String uuid) {
        return new UsidBan(usidBans.find(eq("uuid", uuid)).first());
    }

    public static UsidBan builder() {
        return new UsidBan();
    }

    public boolean isExist() {
        return collection != null;
    }

    public boolean isExpired() {
        return getUnbanTime() <= Time.millis();
    }

    public String getUsid() {
        return collection.usid;
    }

    public UsidBan setUsid(String uuid) {
        collection.usid = uuid;
        return this;
    }

    public String getUuid() {
        return collection.usid;
    }

    public UsidBan setUuid(String uuid) {
        collection.uuid = uuid;
        return this;
    }

    public long getUnbanTime() {
        return collection.unbanTime;
    }

    public UsidBan setUnbanTime(long time) {
        collection.unbanTime = time;
        return this;
    }

    public void delete() {
        usidBans.deleteOne(eq("usid", collection.usid));
    }

    public void commit() {
        usidBans.replaceOne(eq("usid", collection.usid), collection, new ReplaceOptions().upsert(true));
    }
}
