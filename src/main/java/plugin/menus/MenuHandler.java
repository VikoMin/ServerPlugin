package plugin.menus;

import mindustry.gen.Call;
import mindustry.ui.Menus;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import plugin.database.wrappers.PlayerData;
import plugin.models.Ranks;

import static plugin.configs.ConfigJson.discordUrl;


public class MenuHandler {
    public static int welcomeMenu = Menus.registerMenu(((player, option) -> {
        switch (option) {
            case -1, 0 -> {
            }
            case 1 -> Call.openURI(player.con, discordUrl);
        }
    }));
    public static int statsMenu = Menus.registerMenu(((player, option) -> {
        switch (option) {
            case -1, 0 -> {
            }
        }
    }));
    public static int loginMenu;

    public static void loginMenuFunction(MessageCreateEvent listener) {
        loginMenu = Menus.registerMenu(((player, option) -> {
            switch (option) {
                case -1, 1 -> {
                }
                case 0 -> {
                    long discordId = listener.getMessageAuthor().getId();
                    PlayerData data = new PlayerData(player);
                    if (data.getRank() == Ranks.Rank.Player)
                        data.setRank(Ranks.Rank.Verified);
                    data.setDiscordId(discordId);
                    player.sendMessage("[blue]Successfully connected your discord: " + listener.getMessageAuthor().getName());
                    listener.getChannel().sendMessage("Successfully connected your mindustry account!");
                }
            }
        }));
    }

    public static void loginMenuFunction(Message message) {
        loginMenu = Menus.registerMenu(((player, option) -> {
            switch (option) {
                case -1, 1 -> {
                }
                case 0 -> {
                    long discordId = message.getAuthor().getId();
                    PlayerData data = new PlayerData(player);
                    if (data.getRank() == Ranks.Rank.Player)
                        data.setRank(Ranks.Rank.Verified);
                    data.setDiscordId(discordId);
                    player.sendMessage("[blue]Successfully connected your discord: " + message.getAuthor().getName());
                    message.getChannel().sendMessage("Successfully connected your mindustry account!");
                }
            }
        }));
    }
}
