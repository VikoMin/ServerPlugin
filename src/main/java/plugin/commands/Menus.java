package plugin.commands;

import mindustry.gen.Player;
import plugin.etc.Menu;
import plugin.models.wrappers.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class Menus {
    public static void achMenu(Player player) {
        PlayerData data = new PlayerData(player);
        if (data.isExist()) {
            List<String[]> buttons = new ArrayList<>();
            buttons.add(new String[]{"[red]Close", "[purple]Reset prefix"});
            ArrayList<String> achievements = data.getAchievements();
            for (int i = 0; i < achievements.size(); i += 2) {
                List<String> chunk = achievements.subList(i, Math.min(i + 2, achievements.size()));
                buttons.add(chunk.toArray(new String[0]));
            }
            Menu achMenu = new Menu(buttons.toArray(new String[0][0]), "Achievements", "[yellow] Your achievements!", mindustry.ui.Menus.registerMenu(((player1, option) -> {
                switch (option) {
                    case -1, 0 -> {
                    }
                    case 1 -> {
                        // в будущем здесь будет отображение информации о достижении
                    }
                }
            })));
            achMenu.show(player);
        }
    }

}
