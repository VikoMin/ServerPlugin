package plugin.models;

import java.util.Arrays;

public class Ranks {
    public enum Rank {
        None("none", "none", "Rank that does not exist. Uses if cannot get player rank"),
        Player("Player", "[white]Player", "Basic rank that given to all players on our server"),
        Verified("Verified:", "[blue]Verified", "In order to get it you should connect your mindustry account to discord using /login"),
        Moderator("Moderator", "[blue]Moderator", "Moderator, has admin permissions"),
        JS("JS", "[purple]JS", "People that have access to game console and javascript execution"),
        Administrator("Administrator", "[#00bfff]Administrator", "Administrator, highest rank, has access to everything");
        private final String name;
        private final String coloredName;
        private final String description;

        Rank(String name, String coloredName, String description) {
            this.name = name;
            this.coloredName = coloredName;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getColoredName() {
            return coloredName;
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
