package me.regexmc.jdaregexbot.commands.hypixel;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.regexmc.jdaregexbot.gameobjects.bedwars.BedwarsObject;
import me.regexmc.jdaregexbot.gameobjects.bedwars.BedwarsSetter;
import me.regexmc.jdaregexbot.util.Cache;
import me.regexmc.jdaregexbot.util.PageHandler;
import me.regexmc.jdaregexbot.util.Player;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BedwarsCommand extends Command {
    private static long startTime;
    private final EventWaiter waiter;

    public BedwarsCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "bedwars";
        this.cooldown = 10;
        this.arguments = "<ign>";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nGets the Bedwars stats of " + this.arguments;
        this.aliases = new String[]{"bw"};
        this.category = Utils.CommandCategories.HYPIXEL.getCategory();
    }

    private static MessageEmbed[] getStatsEmbed(String playerName) {
        try {
            String UUID = Cache.getUUIDFromCache(playerName);
            if (UUID.equals("Invalid Username")) {
                return new MessageEmbed[]{Utils.invalidUsernameEmbed()};
            }

            Player p = Cache.handle(UUID);
            if (p == null) return new MessageEmbed[]{Utils.invalidUsernameEmbed()};

            BedwarsSetter.setStats(p.getPlayerObject().getJSONObject("stats").getJSONObject("bedwars"), true);
            BedwarsObject bedwarsObject = BedwarsSetter.getBedwarsObject();

            int kills = bedwarsObject.getKills();
            int deaths = bedwarsObject.getDeaths();
            int wins = bedwarsObject.getWins();
            int losses = bedwarsObject.getLosses();

            JSONObject soloObject = bedwarsObject.getBedwarsObject().getJSONObject("solo");
            int soloKills = soloObject.getInt("kills");
            int soloDeaths = soloObject.getInt("deaths");
            int soloWins = soloObject.getInt("wins");
            int soloLosses = soloObject.getInt("losses");

            JSONObject duosObject = bedwarsObject.getBedwarsObject().getJSONObject("duos");
            int duosKills = duosObject.getInt("kills");
            int duosDeaths = duosObject.getInt("deaths");
            int duosWins = duosObject.getInt("wins");
            int duosLosses = duosObject.getInt("losses");

            JSONObject threesObject = bedwarsObject.getBedwarsObject().getJSONObject("threes");
            int threesKills = threesObject.getInt("kills");
            int threesDeaths = threesObject.getInt("deaths");
            int threesWins = threesObject.getInt("wins");
            int threesLosses = threesObject.getInt("losses");

            EmbedBuilder overallEmbed = new EmbedBuilder();
            overallEmbed.setTitle(playerName + "'s Overall Bedwars Stats");
            overallEmbed.setColor(p.getRank().getColor());
            overallEmbed.addField("Kills", kills + "", true);
            overallEmbed.addField("Deaths", deaths + "", true);
            overallEmbed.addField("Wins", wins + "", true);
            overallEmbed.addField("Losses", losses + "", true);
            overallEmbed.setThumbnail("https://hypixel.net/styles/hypixel-v2/images/game-icons/BedWars-64.png");

            EmbedBuilder soloEmbed = new EmbedBuilder();
            soloEmbed.setTitle(playerName + "'s Solo Bedwars Stats");
            soloEmbed.setColor(p.getRank().getColor());
            soloEmbed.addField("Kills", soloKills + "", true);
            soloEmbed.addField("Deaths", soloDeaths + "", true);
            soloEmbed.addField("Wins", soloWins + "", true);
            soloEmbed.addField("Losses", soloLosses + "", true);
            soloEmbed.setThumbnail("https://hypixel.net/styles/hypixel-v2/images/game-icons/BedWars-64.png");

            EmbedBuilder duosEmbed = new EmbedBuilder();
            duosEmbed.setTitle(playerName + "'s Duos Bedwars Stats");
            duosEmbed.setColor(p.getRank().getColor());
            duosEmbed.addField("Kills", duosKills + "", true);
            duosEmbed.addField("Deaths", duosDeaths + "", true);
            duosEmbed.addField("Wins", duosWins + "", true);
            duosEmbed.addField("Losses", duosLosses + "", true);
            duosEmbed.setThumbnail("https://hypixel.net/styles/hypixel-v2/images/game-icons/BedWars-64.png");

            EmbedBuilder threesEmbed = new EmbedBuilder();
            threesEmbed.setTitle(playerName + "'s Threes Bedwars Stats");
            threesEmbed.setColor(p.getRank().getColor());
            threesEmbed.addField("Kills", threesKills + "", true);
            threesEmbed.addField("Deaths", threesDeaths + "", true);
            threesEmbed.addField("Wins", threesWins + "", true);
            threesEmbed.addField("Losses", threesLosses + "", true);
            threesEmbed.setThumbnail("https://hypixel.net/styles/hypixel-v2/images/game-icons/BedWars-64.png");

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            overallEmbed.setFooter("Took " + duration + "ms");
            return new MessageEmbed[]{overallEmbed.build(), soloEmbed.build(), duosEmbed.build(), threesEmbed.build()};
        } catch (IOException | InterruptedException | ExecutionException e) {
            Utils.log(e, Utils.ErrorTypes.ERROR);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            startTime = System.nanoTime();

            if (event.getMessage().getContentRaw().split(" ", 2).length < 2) {
                event.reply("What is the players name?");
                waiter.waitForEvent(MessageReceivedEvent.class,
                        e -> Utils.messageReplyChecks(e, event),
                        e -> {
                            String playerName = e.getMessage().getContentRaw();
                            if (Utils.invalidUsername(playerName)) {
                                event.reply(Utils.invalidUsernameEmbed());
                                return;
                            }
                            event.getChannel().sendMessage(Utils.gettingStatsEmbed()).queue(msg -> {
                                MessageEmbed[] statsEmbeds = getStatsEmbed(playerName);
                                assert statsEmbeds != null;
                                MessageEmbed statsEmbed = statsEmbeds[0];


                                if (statsEmbed.equals(Utils.invalidUsernameEmbed())) {
                                    msg.editMessage(statsEmbed).queue();
                                    return;
                                }

                                msg.editMessage(statsEmbed).queue();

                                PageHandler.managePages(waiter, event.getAuthor(), msg, statsEmbeds, 0, true);
                            });
                        }, 10, TimeUnit.SECONDS, () -> event.reply("No response from " + event.getAuthor().getName()));
                return;
            }

            String playerName = event.getMessage().getContentRaw().split(" ", 2)[1];
            if (Utils.invalidUsername(playerName)) {
                event.reply(Utils.invalidUsernameEmbed());
                return;
            }
            event.getChannel().sendMessage(Utils.gettingStatsEmbed()).queue(msg -> {
                MessageEmbed[] statsEmbeds = getStatsEmbed(playerName);
                assert statsEmbeds != null;


                MessageEmbed statsEmbed = statsEmbeds[0];


                if (statsEmbed.equals(Utils.invalidUsernameEmbed())) {
                    msg.editMessage(statsEmbed).queue();
                    return;
                }

                msg.editMessage(statsEmbed).queue();

                PageHandler.managePages(waiter, event.getAuthor(), msg, statsEmbeds, 0, true);
            });

        }
    }
}
