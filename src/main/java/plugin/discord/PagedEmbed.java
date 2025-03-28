package plugin.discord;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Timer;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import java.awt.*;

public class PagedEmbed {
    private static final Seq<PagedEmbed> embeds = new Seq<>();
    public static void load() {
        Bot.api.addButtonClickListener(event -> {
            var message = event.getButtonInteraction().getMessage();
            var paged = embeds.find(e -> e.message.getId() == message.getId());
            if (paged == null) return;
            event.getButtonInteraction().acknowledge();
            switch (event.getButtonInteraction().getCustomId()) {
                case "min" -> {
                    paged.setPage(1);
                }
                case "max" -> {
                    paged.setPage(paged.pages.size);
                }
                case "next" -> {
                    paged.setPage(Math.min(paged.currentPage + 1, paged.pages.size));
                }
                case "prev" -> {
                    paged.setPage(Math.max(paged.currentPage - 1, 1));
                }
            }
        });
    }



    public int currentPage = 1;
    private final Seq<EmbedPage> pages = new Seq<>();
    public Message message;
    public PagedEmbed() {
        embeds.add(this);
        Timer.schedule(() -> {
            embeds.remove(this);
        }, 1800);
    }
    public PagedEmbed addPage(EmbedPage page) {
        page.page = this.pages.size + 1;
        pages.add(page);
        return this;
    }
    public void setPage(int page) {
        this.currentPage = page;
        this.message.edit(pages.get(page - 1).getEmbed(pages.size));
    }

    public void send(TextChannel channel) {
        channel.sendMessage(pages.get(0).getEmbed(pages.size), ActionRow.of(
                Button.primary("min", "⏮️"),
                Button.primary("prev", "◀️"),
                Button.primary("next", "▶️"),
                Button.primary("max", "⏭️")
        )).whenComplete((msg, err) -> {
            this.message = msg;
        });

    }
    public static class EmbedPage {
        public int page;
        private final EmbedBuilder embed;

        public EmbedPage(String title, Color c) {
            this.embed = new EmbedBuilder().setTitle(title);
            this.embed.setColor(c);
        }
        public EmbedPage addField(String cont, String name) {
            this.embed.addField(name, cont);
            return this;
        }
        public EmbedBuilder getEmbed(int totalPages) {
            return this.embed.setFooter("Page " + this.page + " of " + totalPages);
        }
    }
}
