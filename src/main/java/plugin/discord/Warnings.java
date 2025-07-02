package plugin.discord;

import org.javacord.api.event.message.MessageCreateEvent;

public class Warnings {
    public static void mapNotFound(MessageCreateEvent listener) {
        listener.getChannel().sendMessage("Map not found!");
    }

    public static void noDataFound(MessageCreateEvent listener) {
        listener.getChannel().sendMessage("No PlayerDataCollection found!");
    }
}
