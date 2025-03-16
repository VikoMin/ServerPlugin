package plugin.etc;

import java.util.Arrays;

public class Ranks {
    public enum Rank {
        None("none"),
        Player("[white]PlayerData"),
        Verified("[blue]Verified"),
        Moderator("[blue]Moderator"),
        JS("[purple]JS"),
        Administrator("[#00bfff]Administrator");
        private final String name;

        Rank(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        public boolean equal(Rank rank){
            return this.ordinal() >= rank.ordinal();
        }
    }

    public static Rank getRank(int ordinal){
        if (ordinal < Rank.values().length)
            return Arrays.stream(Rank.values()).toList().get(ordinal);
        return Rank.None;
    }
    public static Rank getRank(String name) {
        switch (name.toLowerCase()) {
            case "player" -> {
                return Rank.Player;
            }
            case "verified" -> {
                return Rank.Verified;
            }
            case "moderator" -> {
                return Rank.Moderator;
            }
            case "js" -> {
                return Rank.JS;
            }
            case "administrator" -> {
                return Rank.Administrator;
            }
            default -> {
                return Rank.None;
            }
        }
    }
}
