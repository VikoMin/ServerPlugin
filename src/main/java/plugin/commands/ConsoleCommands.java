package plugin.commands;

import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Timer;

import org.json.simple.parser.ParseException;
import plugin.discord.Bot;
import plugin.etc.Ranks;
import plugin.models.wrappers.PlayerData;

import java.io.IOException;

import static plugin.ServersConfig.resetServersConfig;

public class ConsoleCommands {
    public static void loadServerCommands(CommandHandler handler) {
        handler.removeCommand("exit");
        handler.register("exit", "exit the server process", (args) -> {
            Bot.api.disconnect();
            Timer.schedule(() -> {
                System.exit(0);
            }, 1f);
        });
        handler.register("setrank", "<id> <rank>", "Sets rank to player", (args, params) -> {
            PlayerData data = new PlayerData(Integer.parseInt(args[0]));
            String rank = args[1];
            if (!data.isExist()) {
                Log.warn("No such player!");
            } else if (Ranks.getRank(rank) == Ranks.Rank.None) {
                Log.warn("This rank doesnt exist!");
            } else {
                data.setRank(rank);
                Log.info("Rank has been given!");
            }
        });
        handler.register("setvip", "<id> <true/false>", "Sets vip to player", (args, params) -> {
            PlayerData data = new PlayerData(Integer.parseInt(args[0]));
            String isVipString = args[1];
            if (!data.isExist()) {
                Log.warn("No such player!");
            } else if (!(isVipString.equals("true") || isVipString.equals("false"))) {
                Log.warn("true or false");
            } else {
                boolean isVip = Boolean.parseBoolean(isVipString);
                data.setVip(isVip);
                Log.info(isVip ? "Given Vip." : "Removed Vip.");
            }
        });
        handler.register("resetserversconfig", "Resets serverConfig.json file", (args) -> {
            try {
                resetServersConfig();
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
