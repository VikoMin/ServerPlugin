package plugin.models;

import mindustry.gen.Player;
import plugin.database.wrappers.PlayerData;

import java.time.Instant;

public class Session {
    private final long id;
    private final String player;
    private final Instant connectionTime;
    private Instant disconnectionTime;
    private int blocksBuilt = 0;
    private int BlocksDestroyed = 0;

    Session(Player player){
        connectionTime = Instant.now();
        this.player = player.uuid();
        id = connectionTime.toEpochMilli() * 10000 + new PlayerData(player).getId();
    }

    public long getId() {
        return id;
    }

    public String getPlayer() {
        return player;
    }

    public void commit(){
        disconnectionTime = Instant.now();
    }
}
