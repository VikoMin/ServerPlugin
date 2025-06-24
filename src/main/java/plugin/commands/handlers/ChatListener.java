package plugin.commands.handlers;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.Player;
import plugin.commands.ChatCommands;
import plugin.commands.annotations.ChatCommand;
import plugin.database.wrappers.PlayerData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ChatListener {
    private static final Map<String, Method> COMMANDS = new HashMap<>();


    private static final ChatCommands LISTENER = new ChatCommands();
    public static final Seq<ChatCommand> commands = new Seq<>();
    static {

        for (Method m : LISTENER.getClass().getDeclaredMethods()) {

            if (m.isAnnotationPresent(ChatCommand.class)) {

                ChatCommand command = m.getAnnotation(ChatCommand.class);
                commands.add(command);

                COMMANDS.put(command.name(), m);

            }
        }
    }

    public static Map<String, Method> getCOMMANDS() {
        return COMMANDS;
    }

    public static void handleCommand(Player player, String message) {
        List<String> args = new ArrayList<>(List.of(message.split(" ")));
        String commandName = args.get(0);
        args = args.subList(1, args.size());
        Method method = COMMANDS.get(commandName);
        if (method == null) {
            player.sendMessage("Command is not exist");
            return;
        }
        Log.info("Command \"" + commandName + "\" called");

        ChatCommand command = method.getAnnotation(ChatCommand.class);
        int maxArgsCount = Math.max(command.minArgsCount(), command.maxArgsCount());
        List<String> resArgs;
        if (!new PlayerData(player).getRank().equal(command.requiredRank())) {
            player.sendMessage("Access denied");
            return;
        } else if (args.size() < command.minArgsCount()) {
            player.sendMessage("Not enough arguments");
            return;
        } else if (args.size() > maxArgsCount && command.isLastArgText()) {
            resArgs = args.subList(0, maxArgsCount - 1);
            resArgs.add(String.join(" ", args.subList(maxArgsCount - 1, args.size())));
            Log.info("Last arg is text");
        } else if (args.size() > maxArgsCount) {
            player.sendMessage("Too many arguments");
            return;
        } else resArgs = args;
        Log.info("Args: " + resArgs);


        try {
            method.invoke(LISTENER, player, resArgs);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            player.sendMessage("Something went wrong");
            Log.info("\n");
            for (StackTraceElement el: exception.getStackTrace())
                Log.info(el);
            Log.info("\n");
        }


    }
}
