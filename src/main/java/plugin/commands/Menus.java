package plugin.commands;

import mindustry.gen.Player;
import plugin.etc.Menu;
import plugin.models.PlayerData;

import java.util.ArrayList;
import java.util.List;

import static plugin.functions.MongoDB.MongoDbUpdate;
import static plugin.utils.FindDocument.getPlayerData;

public class Menus {
    public static void achMenu(Player player){
        PlayerData data = getPlayerData(player.uuid());
        if (data == null) return;
        List<String[]> buttons = new ArrayList<>();
        buttons.add(new String[]{"[red]Close", "[purple]Reset prefix"});
        for (int i = 0; i < data.achievements.size(); i += 2) {
            List<String> chunk = data.achievements.subList(i, Math.min(i+2, data.achievements.size()));
            buttons.add(chunk.toArray(new String[0]));
        }
        Menu achMenu = new Menu(buttons.toArray(new String[0][0]), "Achievements", "[yellow] Your achievements!", mindustry.ui.Menus.registerMenu(((player1, option) -> {
            switch (option){
                case -1,0 -> {}
                case 1 -> {
                    player.sendMessage("Resetted");
                    MongoDbUpdate(data);
                }
                default -> {
                    MongoDbUpdate(data);
                }
            }
        })));
        achMenu.show(player);
    }

}
