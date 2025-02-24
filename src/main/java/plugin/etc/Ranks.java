package plugin.etc;

public class Ranks {
    public enum Perms {
        None(),
        Admin(),
        JS()
    }

    public enum Rank {
        None("none", Perms.None),
        Player("[white]Player", Perms.None),
        Verified("[blue]Verified", Perms.None),
        Moderator("[blue]Moderator", Perms.Admin),
        JS("[purple]JS", Perms.JS),
        Administrator("[#00bfff]Administrator", Perms.JS);
        private final String name;
        private final Perms perms;

        Rank(String name, Perms perms) {
            this.name = name;
            this.perms = perms;
        }

        public String getName() {
            return name;
        }

        public boolean isAdmin() {
            return perms.ordinal() >= Perms.Admin.ordinal();
        }
        public boolean hasJS() {
            return perms.ordinal() >= Perms.JS.ordinal();
        }
    }

    ;

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
