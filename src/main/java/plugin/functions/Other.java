package plugin.functions;

import arc.util.Log;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import mindustry.server.ServerControl;
import plugin.models.PlayerData;
import plugin.utils.MenuHandler;
import useful.Bundle;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;

import static plugin.ConfigJson.discordUrl;
import static plugin.etc.Ranks.getRank;
import static plugin.utils.FindDocument.*;
import static plugin.utils.Utilities.notNullElse;

public class Other {

    public static void welcomeMenu(Player player){
        String title = "\uE86B Добро пожаловать!";
        String description = "[white]Добро пожаловать на [#00bfff]Frost[#99e6ff]Heaven!\n\n" +
                "[red]<[orange]Правила[red]>\n" +
                "[#f]\uE815 [white]Запрещён гриф, слив ресурсов, юнитов и прочие помехи игре.\n" +
                "[#f]\uE815 [white]Запрещён NSFW контент, реклама в любом виде.\n" +
                "[#f]\uE815 [white]Запрещены низкоэффективные и громоздкие схемы.\n" +
                "[green]\uE800 [white]Ведите себя адекватно, не мешайте игре и общению.\n\n" +
                "[white]Используйте /help чтобы увидеть все доступные команды.\n" +
                "[blue]\uE80D И обязательно зайдите на наш дискорд-сервер.";
        String button1 = "Закрыть";
        String button2 = "[blue]\uE80D Перейти в discord!";
        Call.menu(player.con, MenuHandler.welcomeMenu, title, description, new String[][]{{button1}, {button2}});
    }
    public static void kickIfBanned(NetConnection player){
        PlayerData data = getPlayerDataByIP(player.address);
        if (data == null){
            return;
        }
        long lastBan = data.lastBan;
        Date date = new Date();
        if (lastBan > date.getTime()) {
            String timeUntilUnban = Bundle.formatDuration(lastBan - date.getTime());
            player.kick("[red]You have been banned!\n\n" +"[white]Duration: " + timeUntilUnban + " until unban\n\nIf you think this is a mistake, make sure to appeal ban in our discord: " + discordUrl, 0);
        }
    }
    public static void kickIfBanned(Player player){
        PlayerData data = notNullElse(getPlayerDataAnyway(player.uuid()), getPlayerDataAnyway(player.ip()));
        if (data == null){
            return;
        }
        long lastBan = data.lastBan;
        Date date = new Date();
        if (lastBan > date.getTime()) {
            String timeUntilUnban = Bundle.formatDuration(lastBan - date.getTime());
            player.kick("[red]You have been banned!\n\n" +"[white]Duration: " + timeUntilUnban + " until unban\n\nIf you think this is a mistake, make sure to appeal ban in our discord: " + discordUrl, 0);
        }
    }
    public static void statsMenu(Player player, Player reqPlayer){
        PlayerData data = getPlayerData(reqPlayer.uuid());
        String rank = getRank(data.rank).name();
        String title = "\uE86B Stats";
        long playtime = Long.parseLong(String.valueOf(data.playtime));
        String description  = "[orange]Name: " + reqPlayer.name()
        + "\n[orange]ID: [white]" + data.id
        + "\n[orange]Rank: " + rank
        + "\n[orange]Achievements: [white]"+ data.achievements
        + "\n\n[orange]Playtime: [white]" + Bundle.formatDuration(Duration.ofMinutes(playtime));
        String button = "[red]Close";
        Call.menu(player.con, MenuHandler.statsMenu, title, description, new String[][]{{button}});
    }
    public static  String readValueFromArraySeparated(String[] array, int startPoint, int endPoint){
        String[] newArray = Arrays.copyOfRange(array, startPoint, endPoint);
        StringBuilder ha = new StringBuilder();
        for (String element : newArray){
            ha.append(element).append(" ");
        }
        String a = String.valueOf(ha);
        return a.substring(0, a.length()-1);
    }
    public static <T> boolean inBounds(T[] array, int index){
        return (index >= 0) && (index < array.length);
    }
    public static void reloadMaps(){
        ServerControl.instance.handleCommandString("reloadmaps");
        Log.info("Maps has been reloaded!");
    }
}
