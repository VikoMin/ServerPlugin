package plugin.database.collections;

import java.util.ArrayList;

public class PlayerData {
    public int id;
    public String uuid;
    public ArrayList<String> names = new ArrayList<>();
    public String rawName = "<none>";
    public int rank = 0;
    public String joinMessage = "@ joined!";
    public ArrayList<String> ips = new ArrayList<>();
    public long lastBan = 0;
    public long discordId = 0;
    public long playtime = 0;
    public boolean isVip = false;
    public ArrayList<String> adminUsids = new ArrayList<>();

    public PlayerData() {
    } //necessary for mongodb

    public PlayerData(int id, String uuid) {
        this.uuid = uuid;
        this.id = id;
    }
}
