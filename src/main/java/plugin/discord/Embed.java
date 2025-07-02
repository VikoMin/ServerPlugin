package plugin.discord;

import mindustry.gen.Player;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import plugin.Utilities;
import plugin.database.wrappers.PlayerData;
import useful.Bundle;

import java.awt.*;
import java.time.Duration;

public class Embed {

    public static EmbedBuilder banEmbed(Player player, Player moderator, String reason, long banTime) {
        PlayerData data = new PlayerData(player);
        return new EmbedBuilder()
                .setTitle("Ban event")
                .setColor(Color.RED)
                .addField("**ID**", String.valueOf(data.getId()))
                .addField("**Name**", player.plainName())
                .addField("**UUID**", player.uuid())
                .addField("**IP**", data.getIPs().toString())
                .addField("**Reason**", reason)
                .addField("**Expires**", "<t:" + banTime / 1000 + ":D>")
                .addField("**Moderator**", moderator.plainName())
                .addField("**Moderator ID**", String.valueOf(new PlayerData(moderator).getId()));
    }

    public static EmbedBuilder banEmbed(PlayerData player, MessageAuthor moderator, String reason, long banTime) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Ban event")
                .setColor(Color.RED)
                .addField("**ID**", String.valueOf(player.getId()))
                .addField("**Name**", player.getNames().get(player.getNames().size() - 1))
                .addField("**UUID**", player.getUuid())
                .addField("**IP**", player.getIPs().toString())
                .addField("**Reason**", reason)
                .addField("**Expires**", "<t:" + banTime / 1000 + ":D>")
                .addField("**Moderator**", moderator.getName());

        long id = PlayerData.getIdBySnowFlake(moderator.getId());
        if (id != moderator.getId()) embed.addField("**Moderator ID**", String.valueOf(id));
        return embed;
    }

    public static EmbedBuilder infoEmbed(PlayerData data) {
        return new EmbedBuilder()
                .setTitle("Player info")
                .setColor(Color.CYAN)
                .addField("**ID**", String.valueOf(data.getId()))
                .addField("**UUID**", data.getUuid())
                .addField("**Names**", Utilities.stringify(data.getNames(), name -> "\n- " + name))
                .addField("**IPs**", Utilities.stringify(data.getIPs(), name -> "\n- " + name))
                .addField("**Playtime**", Bundle.formatDuration(Duration.ofMinutes(data.getPlaytime())));
    }
}
