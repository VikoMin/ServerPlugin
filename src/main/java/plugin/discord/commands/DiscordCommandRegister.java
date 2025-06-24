package plugin.discord.commands;

import arc.func.Cons2;
import arc.struct.Seq;
import arc.util.Log;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import plugin.Utilities;
import plugin.configs.ConfigJson;


public class DiscordCommandRegister {

    private static final String prefix = ".";
    public static Seq<DiscordCommand> commands = new Seq<>();
    private final DiscordCommand c;

    public DiscordCommandRegister(String name) {
        c = new DiscordCommand();
        c.name = name;
    }

    public static DiscordCommandRegister create(String name) {
        return new DiscordCommandRegister(name);
    }

    public static void handleMessage(Message message) {
        if (message == null) return;
        String text = message.getContent();
        User author = message.getUserAuthor().orElse(null);
        if (author == null) return;
        if (!text.startsWith(ConfigJson.prefix)) return;
        text = text.substring(ConfigJson.prefix.length());
        String[] parts = text.split(" ");
        for (DiscordCommand command : commands) {
            if (command.name.equals(parts[0])) {
                if (!command.allowedRoles.isEmpty()) {
                    var server = message.getServer().orElse(null);
                    if (server == null) return;
                    if (!Utilities.haveRole(author.getRoles(server), command.allowedRoles)) return;
                }
                text = text.replaceFirst(command.name, "").trim();
                if (text.split(" ").length < command.requiredArgs) {
                    message.getChannel().sendMessage("Too few arguments! Usage: " + command.args);
                    return;
                }
                try {
                    command.acceptor.get(message, text);
                    break;
                } catch (Throwable e) {
                    Log.err(e);
                    message.getChannel().sendMessage("An internal error occured while running this command.");
                    return;
                }
            }
        }
    }

    public DiscordCommandRegister addRole(long role) {
        c.allowedRoles.add(role);
        return this;
    }

    public DiscordCommandRegister addRole(String role) {
        c.allowedRoles.add(Long.parseLong(role));
        return this;
    }

    public DiscordCommandRegister args(String args) {
        c.args = args;
        return this;
    }

    public DiscordCommandRegister requiredArgs(int h) {
        c.requiredArgs = h;
        return this;
    }

    public DiscordCommandRegister desc(String desc) {
        c.desc = desc;
        return this;
    }

    public void build(Cons2<Message, String> acc) {
        c.acceptor = acc;
        commands.add(c);
    }

    public DiscordCommandRegister hidden(boolean hidden) {
        c.hidden = hidden;
        return this;
    }

}
