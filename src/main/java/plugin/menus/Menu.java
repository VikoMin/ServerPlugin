package plugin.menus;

import mindustry.gen.Call;
import mindustry.gen.Player;

public class Menu {
    public String[][] buttons;
    public String title;
    public String message;
    public int listener;
    public Menu(String[][] buttons, String title, String message, int listener){
        this.buttons = buttons;
        this.title = title;
        this.message = message;
        this.listener = listener;
    }
    public void show(Player player){
        Call.menu(player.con, listener, title, message, buttons);
    }
}
