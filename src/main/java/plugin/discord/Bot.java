package plugin.discord;

import arc.Events;
import arc.util.*;
import arc.util.Timer;
import mindustry.game.EventType;
import mindustry.gen.Call;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.event.message.MessageCreateEvent;
import plugin.ConfigJson;
import plugin.discord.commands.DiscordCommandRegister;
import plugin.models.wrappers.PlayerData;


@SuppressWarnings("unused")
public class Bot {
    // variables for load function
    public static DiscordApi api;
    public static TextChannel channel;
    public static TextChannel banchannel;
    public static String prefix = ConfigJson.prefix;

    // main bot
    public static void load() {
        api = new DiscordApiBuilder().setToken(ConfigJson.token).addIntents(Intent.GUILDS, Intent.MESSAGE_CONTENT, Intent.GUILD_MESSAGES).login().join();
        api.addMessageCreateListener(Bot::onMessageCreate);
        channel = api.getChannelById(ConfigJson.logChannelId).get().asTextChannel().get();
        banchannel = api.getChannelById(ConfigJson.banLogChannelId).get().asTextChannel().get();
        init();
    }

    // the stuff that logs if bot is started and also some random events
    public static void init() {
        Log.info("Bot started");
        Commands.load();
        Events.on(EventType.PlayerChatEvent.class, event -> {
            if (event.message.startsWith("/")) {
                return;
            }
            channel.sendMessage(event.player.plainName() + ": `" + event.message + "`");
        });

        Events.on(EventType.PlayerJoin.class, event -> Timer.schedule(() -> {
            PlayerData data = new PlayerData(event.player);
            if (data.isExist()) {
                channel.sendMessage("`" + event.player.plainName() + " (" + data.getId() + ")" + " joined the server!" + "`");
            }
        }, 0.2f));

        Events.on(EventType.PlayerLeave.class, event -> Timer.schedule(() -> {
            PlayerData data = new PlayerData(event.player);
            channel.sendMessage("`" + event.player.plainName() + " (" + data.getId() + ")" + " left the server!" + "`");
        }, 0.2f));
    }

    // creating listener once message is created
    private static void onMessageCreate(MessageCreateEvent listener) {
        /*if(!state.is(GameState.State.playing)){
            listener.getChannel().sendMessage("Server is not running");
            returnl
        }
        if(listener.getServer().isEmpty()){
            listener.getChannel().sendMessage("Cant use commands in DM's");
            return;
        }*/
        if (listener.getChannel() == channel && listener.getMessageAuthor().isRegularUser()) {
            Call.sendMessage("[blue][white] > " + listener.getMessageAuthor().getName() + "[white]: " + listener.getMessageContent());
        }
        if (listener.getMessageAuthor().isBotUser()) {
            return;
        }
        DiscordCommandRegister.handleMessage(listener.getMessage());
    }
}
// registers slash commands so user can see them and use
    /*private static void registerSlashCommands() {
        SlashCommand banCommand = SlashCommand.with("ban", "Bans the player",
                        Arrays.asList(
                                SlashCommandOption.create(
                                        SlashCommandOptionType.STRING,
                                        "idOrNameOrUUID",
                                        "id or name or uuid of the player",
                                        true
                                ),
                                SlashCommandOption.create(
                                        SlashCommandOptionType.LONG,
                                        "time",
                                        "Duration of ban (in days)",
                                        true
                                ),
                                SlashCommandOption.create(
                                        SlashCommandOptionType.STRING,
                                        "reason",
                                        "Reason of ban",
                                        true
                                )
                        )
        ).createGlobal(api).join();
        SlashCommand listCommand = SlashCommand.with("list", "Lists the players"
        ).createGlobal(api).join();
        SlashCommand adminaddCommand = SlashCommand.with("adminadd", "gives admin to player (use it carefully)",
                        Collections.singletonList(
                                SlashCommandOption.create(
                                        SlashCommandOptionType.STRING,
                                        "name",
                                        "name of the player",
                                        true
                                ))
        ).createGlobal(api).join();
        SlashCommand gameoverCommand = SlashCommand.with("gameover", "Executes gameover event"
        ).createGlobal(api).join();
        SlashCommand loginCommand = SlashCommand.with("login", "Connects your discord and mindustry account!",
                Collections.singletonList(
                        SlashCommandOption.create(
                                SlashCommandOptionType.STRING,
                                "idOrName",
                                "id or name of player",
                                true
                        ))
        ).createGlobal(api).join();
        SlashCommand getInfoCommand = SlashCommand.with("stats", "Gets stats of player",
                Collections.singletonList(
                        SlashCommandOption.create(
                                SlashCommandOptionType.STRING,
                                "idOrName",
                                "PlayerData id or name",
                                true
                        ))
        ).createGlobal(api).join();
        SlashCommand searchCommand = SlashCommand.with("search", "Searchs the players in db",
                Collections.singletonList(
                        SlashCommandOption.create(
                                SlashCommandOptionType.STRING,
                                "name",
                                "PlayerData name",
                                true
                        ))
        ).createGlobal(api).join();

        SlashCommand unbanCommand = SlashCommand.with("unban", "Unbans the player",
                        Collections.singletonList(
                                SlashCommandOption.create(
                                        SlashCommandOptionType.STRING,
                                        "idOrName",
                                        "id or name of the player",
                                        true
                                )
                        )
        ).createGlobal(api).join();
        SlashCommand cmdCommand = SlashCommand.with("js", "Execute js command",
                Collections.singletonList(
                        SlashCommandOption.create(
                                SlashCommandOptionType.STRING,
                                "cmd",
                                "The command you want to execute",
                                true
                        ))
        ).createGlobal(api).join();
    }
    // calling slash command functions once they got used
    private static void addSlashCommandListener(SlashCommandCreateEvent listener) {
        if(!state.is(GameState.State.playing)){
            listener.getSlashCommandInteraction().createImmediateResponder().setContent("Server is not running.").respond();
            return;
        }
        if(listener.getSlashCommandInteraction().getServer().isEmpty()){
            listener.getSlashCommandInteraction().createImmediateResponder().setContent("Cant use commands in DM").respond();
        }
        switch(listener.getSlashCommandInteraction().getCommandName()){
            case "ban" -> {
                if (!isModerator(listener)){
                    return;
                }

                String response;

                String id = listener.getSlashCommandInteraction().getOptionByName("idOrNameOrUUID").get().getStringValue().get();
                String reason = listener.getSlashCommandInteraction().getOptionByName("reason").get().getStringValue().get();
                long time = listener.getSlashCommandInteraction().getOptionByName("time").get().getLongValue().get();
                Date date = new Date();
                long banTime = date.getTime() + TimeUnit.DAYS.toMillis(time);
                String timeUntilUnban = Bundle.formatDuration(Duration.ofDays(time));
                Document user = getDocAnyway(id);
                if (user == null) {
                    response = "Could not find that player.";
                    listener.getSlashCommandInteraction()
                            .createImmediateResponder().setContent(response)
                            .respond();
                    return;
                }
                PlayerData plr = Groups.player.find(p -> p.uuid().equals(user.getString("uuid")));
                if (plr == null) {
                    Log.info("PlayerData is offline, not kicking him");
                } else {
                    plr.con.kick("[red]You have been banned!\n\n" + "[white]Reason: " + reason + "\nDuration: " + timeUntilUnban + " until unban\nIf you think this is a mistake, make sure to appeal ban in our discord: " + discordurl, 0);
                }
                listener.getSlashCommandInteraction()
                        .createImmediateResponder().setContent("Banned: " + user.getString("name"))
                        .respond();

                Call.sendMessage(user.getString("name") + " has been banned for: " + reason);
                MongoDbUpdate(user, Updates.set("lastBan", banTime));
                Bot.banchannel.sendMessage(banEmbed(user, reason, banTime, listener.getInteraction().getUser().getName()));
            }
            case "adminadd" -> {
                if (!isModerator(listener)){
                    return;
                }
                String name = listener.getSlashCommandInteraction().getOptionByName("name").get().getStringValue().get();
                PlayerData player = findPlayerByName(name);
                if (player == null){
                    listener.getSlashCommandInteraction().createImmediateResponder().setContent("No such player!").respond(); return;
                }
                if (player.admin()){
                    listener.getSlashCommandInteraction().createImmediateResponder().setContent("PlayerData is already admin!").respond(); return;
                }
                netServer.admins.adminPlayer(String.valueOf(player.uuid()), player.usid());
                listener.getSlashCommandInteraction().createImmediateResponder().setContent("Successfully admin " + player.plainName()).respond();
            }
            case "gameover" -> {
                if (!isModerator(listener)){
                    return;
                }
                Events.fire(new EventType.GameOverEvent(Team.derelict));
                listener.getSlashCommandInteraction().createImmediateResponder().setContent("Gameover executed!").respond();
            }
            case "login" -> {
                String id = listener.getSlashCommandInteraction().getOptionByName("idOrName").get().getStringValue().get();
                Document user = getDocAnyway(id);
                if (user == null){
                    listener.getSlashCommandInteraction().createImmediateResponder().setContent("This player doesnt exists!").respond();
                    return;
                }
                PlayerData player = Groups.player.find(p -> p.uuid().equals(user.getString("uuid")));
                if (player == null){
                    listener.getSlashCommandInteraction().createImmediateResponder().setContent("This player is offline!").respond();
                    return;
                }
                loginMenuFunction(listener);
                Call.menu(player.con, loginMenu, "Request", listener.getInteraction().getUser().getName() + " requests to connect your mindustry account", new String[][]{{"Connect"}, {"Cancel"}});
                listener.getSlashCommandInteraction().createImmediateResponder().setContent("req sended!").respond();
            }
            case "stats" -> {
                String id = listener.getSlashCommandInteraction().getOptionByName("idOrName").get().getStringValue().get();
                Document user = getDocAnyway(id);
                if (user == null){
                    listener.getSlashCommandInteraction().createImmediateResponder().setContent("Could not find that player!").respond();
                    return;
                }
                long playtime = Long.parseLong(String.valueOf(user.getInteger("playtime")));
                String discordId = String.valueOf(user.getLong("discordid"));
                if (discordId.equals("0")){
                    discordId = "none";
                }
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Information")
                        .setColor(Color.RED)
                        .addField("Name", stripColors(user.getString("name")))
                        .addField("ID", String.valueOf(user.getInteger("id")))
                        .addField("Rank", String.valueOf(user.getInteger("rank")))
                        .addField("Playtime",  Bundle.formatDuration(Duration.ofMinutes(playtime)))
                        .addField("Discord (if linked)", "<@" +discordId +">");
                listener.getSlashCommandInteraction().createImmediateResponder().addEmbed(embed).respond();
            }
            case "search" -> {
                String name= listener.getSlashCommandInteraction().getOptionByName("name").flatMap(SlashCommandInteractionOption::getStringValue).orElse("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
                StringBuilder list = new StringBuilder();
                Pattern pattern = Pattern.compile(".?" +name + ".?", Pattern.CASE_INSENSITIVE);
                list.append("```Results:\n\n");
                try (MongoCursor<Document> cursor = plrCollection.find(Filters.regex("name", pattern)).limit(25).iterator()) {
                    while (cursor.hasNext()) {
                        Document csr = cursor.next();
                        list.append(csr.get("name")).append("; ID: ").append(csr.get("id")).append("\n");
                    }
                }
                list.append("```");
                listener.getSlashCommandInteraction().createImmediateResponder().setContent(String.valueOf(list)).respond();
            }
            case "unban" -> {
                if (!isModerator(listener)){
                    return;
                }
                String id = listener.getSlashCommandInteraction().getOptionByName("idOrName").get().getStringValue().get();
                Document user = getDocAnyway(id);
                if (user == null){
                    listener.getSlashCommandInteraction().createImmediateResponder().setContent("Could not find that player!").respond();
                    return;
                }
                if (user.getLong("lastBan") == 0L){
                    listener.getSlashCommandInteraction().createImmediateResponder().setContent("User is not banned!").respond();
                    return;
                }
                MongoDbUpdate(user, Updates.set("lastBan", 0L));
                listener.getSlashCommandInteraction().createImmediateResponder().setContent(user.getString("name") + " has been unbanned!").respond();
            }
            case "js" -> {
                if (!isAdmin(listener)){
                    return;
                }
                String cmd = listener.getSlashCommandInteraction().getOptionByName("cmd").get().getStringValue().get();
                Core.app.post(() -> {
                    String output = mods.getScripts().runConsole(cmd);
                    listener.getSlashCommandInteraction().createImmediateResponder().setContent(output).respond();
                });
            }
          }
        }*/

