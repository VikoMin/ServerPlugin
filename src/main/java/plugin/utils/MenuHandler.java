package plugin.utils;

import mindustry.gen.Call;
import mindustry.ui.Menus;
import org.javacord.api.event.message.MessageCreateEvent;
import plugin.models.PlayerData;

import static plugin.ConfigJson.discordUrl;
import static plugin.functions.MongoDB.MongoDbUpdate;
import static plugin.utils.FindDocument.getPlayerData;


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
                    PlayerData data = getPlayerData(player.uuid());
                    if (data.rank.equals("player")) {
                        data.discordId = discordId;
                        data.rank = "trusted";
                    } else {
                        data.discordId = discordId;
                    }
                    MongoDbUpdate(data);
                    player.sendMessage("[blue]Successfully connected your discord: " + listener.getMessageAuthor().getName());
                    listener.getChannel().sendMessage("Successfully connected your mindustry account!");
                }
            }
        }));
    }
}
