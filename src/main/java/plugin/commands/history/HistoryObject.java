package plugin.commands.history;

import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.Tile;
import plugin.database.wrappers.PlayerData;

import java.util.Date;

public class HistoryObject {
    public final Tile tile;
    public final Block actionBlock;
    public final Player actionPlayer;
    public final int playerId;
    public final String action;
    public final Date time;

    public HistoryObject(Tile tile, Block actionBlock, Player actionPlayer, String action, Date time) {
        this.tile = tile;
        this.actionBlock = actionBlock;
        this.actionPlayer = actionPlayer;
        this.playerId = new PlayerData(actionPlayer).getId();
        this.action = action;
        this.time = time;
    }
}
