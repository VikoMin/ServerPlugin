package plugin.discord.commands;

import arc.func.Cons2;
import org.javacord.api.entity.message.Message;

import java.util.HashSet;
import java.util.Set;

public class DiscordCommand {
    public Set<Long> allowedRoles = new HashSet<>();
    public String name;
    public String args = "";
    public String desc;
    public boolean hidden = false;
    public Cons2<Message, String> acceptor;
    int requiredArgs = 0;

    public DiscordCommand() {
    }
}
