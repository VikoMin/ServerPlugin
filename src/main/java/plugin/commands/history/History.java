package plugin.commands.history;

import arc.Events;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import useful.Bundle;

import java.util.Date;

import static mindustry.Vars.emptyTile;
import static mindustry.Vars.world;

public class History {
    public static Seq<String> historyPlayers = new Seq<>();
    static Seq<HistoryTile> historyTilesSeq = new Seq<>();

    public static void loadHistory() {
        Events.on(EventType.BlockBuildEndEvent.class, event -> {
            int block = event.tile.build instanceof ConstructBlock.ConstructBuild build ? build.current.id : event.tile.blockID();
            if (!event.unit.isPlayer()) return;
            add(new HistoryObject(event.tile, Vars.content.block(block), event.unit.getPlayer(), (event.breaking) ? "broke" : "built", new Date()));
        });
        Events.on(EventType.BuildRotateEvent.class, event -> {
            int block = event.build instanceof ConstructBlock.ConstructBuild build ? build.current.id : event.build.tile.blockID();
            if (!event.unit.isPlayer()) return;
            HistoryObject obj = new HistoryObject(event.build.tile, Vars.content.block(block), event.unit.getPlayer(), "rotated", new Date());
            add(obj);
        });
        Events.on(EventType.ConfigEvent.class, event -> {
            int block = event.tile instanceof ConstructBlock.ConstructBuild build ? build.current.id : event.tile.block().id;
            if (event.player == null) return;
            HistoryObject obj = new HistoryObject(event.tile.tile, Vars.content.block(block), event.player, "configured", new Date());
            add(obj);
        });
        /*Events.on(EventType.TapEvent.class, event -> {
            if (!historyPlayers.contains(event.player.uuid())){
                return;
            }
            StringBuilder list = new StringBuilder();
            list.append("[orange]Tile history:[white]\n");
            HistoryTile tile = getTile(event.tile);
            Seq<HistoryObject> reversed = new Seq<>(tile.objectSeq);
            reversed.reverse();
            if (reversed.isEmpty()){
                list.append("[red]No actions has been done on this tile!");
            } else {
                for (HistoryObject obj : reversed) {
                    if (obj.tile == Vars.world.tileWorld(event.player.mouseX, event.player.mouseY)) {
                        list.append(format(obj));
                    }
                }
            }
            event.player.sendMessage(list.toString());
        });*/
        Events.run(EventType.Trigger.update, () -> {
            for (Player plr : Groups.player) {
                if (historyPlayers.contains(plr.uuid())) {
                    Tile Eventtile = world.tileWorld(plr.mouseX, plr.mouseY);
                    if (Eventtile != null) {
                        StringBuilder list = new StringBuilder();
                        HistoryTile tile = getTile(Eventtile);
                        Seq<HistoryObject> reversed = new Seq<>(tile.objectSeq);
                        reversed.reverse();
                        for (HistoryObject obj : reversed) {
                            if (obj.tile == Vars.world.tileWorld(plr.mouseX, plr.mouseY)) {
                                list.append(format(obj));
                            }
                        }
                        list.append("[").append(Eventtile.x).append(", ").append(Eventtile.y).append("]");
                        /*Call.infoPopup(plr.con(), list.toString(), 0.017f, Align.center | Align.left, 0, 0, 0, 0);*/
                        Call.setHudText(plr.con(), list.toString());
                    }
                }
            }
        });
        Events.on(EventType.GameOverEvent.class, event -> historyTilesSeq.clear());
    }

    public static void add(HistoryObject obj) {
        if (obj.tile == emptyTile) return;
        obj.tile.getLinkedTiles(other -> {
            HistoryObject newObj = new HistoryObject(other, obj.actionBlock, obj.actionPlayer, obj.action, obj.time);
            HistoryTile realTile = getTile(other);
            HistoryTile newTile = getTile(other);
            Seq<HistoryObject> objectSeq = new Seq<>(realTile.objectSeq);
            objectSeq.add(newObj);
            if (objectSeq.size > 7) {
                objectSeq.remove(0);
            }
            newTile.objectSeq = objectSeq;
            if (historyTilesSeq.contains(realTile)) {
                historyTilesSeq.replace(realTile, newTile);
            } else {
                historyTilesSeq.add(newTile);
            }
        });
    }

    public static String format(HistoryObject obj) {
        return Bundle.formatDateTime(obj.time) + " " + obj.actionPlayer.name() + "[grey] [" + obj.playerId + "][white] " + obj.action + " " + obj.actionBlock.emoji() + "\n";
    }

    public static HistoryTile getTile(Tile tile) {
        for (HistoryTile histTile : historyTilesSeq) {
            if (histTile.tileX == tile.worldx() && histTile.tileY == tile.worldy()) {
                return histTile;
            }
        }
        return new HistoryTile(tile.worldx(), tile.worldy());
    }
}
