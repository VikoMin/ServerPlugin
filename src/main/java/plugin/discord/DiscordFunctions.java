package plugin.discord;

import mindustry.Vars;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static plugin.ConfigJson.adminId;
import static plugin.ConfigJson.moderatorId;
import static plugin.discord.Embed.noRoleEmbed;

public class DiscordFunctions {
    public static boolean isModerator(MessageCreateEvent listener){
        User user =  listener.getMessageAuthor().asUser().get();
        org.javacord.api.entity.server.Server server = listener.getServer().get();
        Optional<Role> moderatorRole = server.getRoleById(moderatorId);
        List<Role> roles = user.getRoles(server);
        if (roles.contains(moderatorRole.get())){
            return true;
        } else {
            listener.getChannel().sendMessage(noRoleEmbed(moderatorRole));
            return false;
        }
    }
    public static boolean isAdmin(MessageCreateEvent listener){
        User user =  listener.getMessageAuthor().asUser().get();
        org.javacord.api.entity.server.Server server = listener.getServer().get();
        Optional<Role> adminRole = server.getRoleById(adminId);
        List<Role> roles = user.getRoles(server);
        if (roles.contains(adminRole.get())){
            return true;
        } else {
            listener.getChannel().sendMessage(noRoleEmbed(adminRole));
            return false;
        }
    }
    public static void createAndSendTempFile(MessageCreateEvent listener, List<String> list) throws IOException {
        File readFile = new File(Vars.tmpDirectory.absolutePath() + "/readfile.txt");
        readFile.createNewFile();
        FileWriter writer = new FileWriter(readFile);
        for (String line : list){
            writer.write(line + "\n");
        }
        writer.close();
        listener.getChannel().sendMessage(readFile);
        listener.getChannel().sendMessage("done");
    }
    public static void createAndSendTempFile(Message msg, List<String> list) throws IOException {
        File readFile = new File(Vars.tmpDirectory.absolutePath() + "/readfile.txt");
        readFile.createNewFile();
        FileWriter writer = new FileWriter(readFile);
        for (String line : list){
            writer.write(line + "\n");
        }
        writer.close();
        msg.getChannel().sendMessage(readFile);
    }
}
