package plugin.models;

import mindustry.gen.Player;
import plugin.database.wrappers.PlayerData;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoField;

public class Session {
    private final long id;
    private final Instant connectionTime;
    private int messages = 0;
    private int blocksBuilt = 0;
    private int blocksDestroyed = 0;

    Session(Player player){
        connectionTime = Instant.now();
        id = new PlayerData(player).getId();
    }

    public void increaseBlocks(boolean broke){
        if (broke)
            blocksDestroyed++;
        else
            blocksBuilt++;
    }

    public void increaseMessages(){
        messages++;
    }

    public void commit(){
        Instant disconnectionTime = Instant.now();
        PlayerData player = new PlayerData(((int) id));
        player.increasePlaytime(disconnectionTime.get(ChronoField.INSTANT_SECONDS) - connectionTime.get(ChronoField.INSTANT_SECONDS));
        try {
            FileWriter csv = new FileWriter("sessions.csv");
            csv.append(String.valueOf(id)).append(";").
                    append(String.valueOf(connectionTime)).append(";").
                    append(String.valueOf(connectionTime)).append(";").
                    append(String.valueOf(messages)).append(";").
                    append(String.valueOf(blocksBuilt)).append(";").
                    append(String.valueOf(blocksDestroyed)).append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
