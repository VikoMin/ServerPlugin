package plugin.models;

import java.util.ArrayList;
public class PlayerData {
    public String uuid;
    public int id;
    public String name = "<none>";
    public String rawName = "<none>";
    public String rank = "player";
    public String joinMessage = "@ joined!";
    public String ip = "<none>";
    public long lastBan = 0;
    public long discordId = 0;
    public ArrayList<String> achievements = new ArrayList<>();
    public int playtime = 0;
    public boolean isVip = false;
    public PlayerData(){

    }
    public PlayerData(String uuid, int id){
        this.uuid = uuid;
        this.id = id;
    }
}
