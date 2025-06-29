package plugin;

import arc.Events;
import arc.func.Cons;
import arc.func.Func;
import arc.graphics.Pixmap;
import arc.struct.Seq;
import arc.util.Reflect;
import arc.util.Threads;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import org.javacord.api.entity.permission.Role;
import rhino.Context;
import rhino.NativeJavaObject;
import rhino.Scriptable;
import rhino.Undefined;

import java.util.ArrayList;
import java.util.Set;

import static mindustry.io.MapIO.colorFor;
import static plugin.commands.ChatCommands.votedPlayer;
import static plugin.commands.ChatCommands.votes;

public class Utilities {
    private static final Scriptable scope = Reflect.get(Vars.mods.getScripts(), "scope");

    // finds player using their name (without colors)
    public static Player findPlayerByName(String name) {
        return Groups.player.find(t -> t.plainName().contains(name));
    }

    public static void voteCanceled() {
        Call.sendMessage("[red]Vote has been canceled!");
        votes.set(0);
        votedPlayer.clear();
    }

    public static void voteSuccess(Map map) {
        Call.sendMessage("[green]Vote success! Changing map!");
        Vars.maps.setNextMapOverride(map);
        Events.fire(new EventType.GameOverEvent(Team.derelict));
        votes.set(0);
        votedPlayer.clear();
    }

    public static Seq<Map> getMaps() {
        return Vars.maps.customMaps().copy();
    }

    public static <T> String stringify(ArrayList<T> arr, Func<T, String> stringer) {
        if (arr == null || arr.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        for (T elem : arr) {
            out.append(stringer.get(elem));
        }
        return out.toString();
    }

    public static void runJs(String code, Cons<String> result) {
        Threads.thread(() -> {
            Object out;
            try {
                Object resp = Context.enter().evaluateString(scope, code, "console.js", 1);
                if (resp instanceof NativeJavaObject o) {
                    out = o.unwrap();
                } else if (resp instanceof Undefined) {
                    out = "undefined";
                } else {
                    out = String.valueOf(resp).trim();
                }
            } catch (Exception e) {
                out = e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : " ");
            }
            Context.exit();
            result.get(out != null ? out.toString() : "null");
        });
    }

    public static boolean haveRole(java.util.List<org.javacord.api.entity.permission.Role> roles, Set<Long> required) {
        for (Role r : roles) {
            if (required.contains(r.getId())) return true;
        }
        return false;
    }

    public static <T> Seq<Seq<T>> splitBy(Seq<T> arr, int cap) {
        Seq<Seq<T>> res = new Seq<>();
        int start = 0;
        for (int i = 0; i < (arr.size + cap - 1) / cap; i++) {
            int end = Math.min(start + cap, arr.size);
            var s = new Seq<T>();
            for (int j = start; j < end; j++) {
                s.add(arr.get(j));
            }
            res.add(s);
            start = end;
        }
        return res;
    }

    /**
     * Получить рендер карт с цветом предмета в конфиге блока(сортер и т.д.)
     *
     * @param tiles Тайлы карты
     * @return Pixmap для сохранения в файл
     */
    public static Pixmap generatePreview(Tiles tiles) {
        Pixmap pixmap = new Pixmap(tiles.width, tiles.height);
        for (int x = 0; x < pixmap.width; x++) {
            for (int y = 0; y < pixmap.height; y++) {
                Tile tile = tiles.getn(x, y);
                if (tile.build != null) {
                    if (tile.build.config() instanceof Item) {
                        Item item = (Item) tile.build.config();
                        pixmap.set(x, pixmap.height - 1 - y, item.color.rgba());
                        item = null;
                    } else {
                        pixmap.set(x, pixmap.height - 1 - y, colorFor(tile.block(), tile.floor(), tile.overlay(), tile.team()));
                    }
                } else {
                    pixmap.set(x, pixmap.height - 1 - y, colorFor(tile.block(), tile.floor(), tile.overlay(), tile.team()));
                }
            }
        }
        return pixmap;
    }
}

