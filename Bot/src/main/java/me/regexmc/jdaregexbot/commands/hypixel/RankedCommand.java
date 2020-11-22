package me.regexmc.jdaregexbot.commands.hypixel;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.regexmc.jdaregexbot.util.Cache;
import me.regexmc.jdaregexbot.util.PageHandler;
import me.regexmc.jdaregexbot.util.Player;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RankedCommand extends Command {
    private static long startTime;
    private final EventWaiter waiter;

    public RankedCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "ranked";
        this.cooldown = 10;
        this.arguments = "<ign>";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nGets the Ranked stats of " + this.arguments;
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

            TreeMap<Integer, int[]> rankedSeasons = Utils.getSeasons((JSONObject) ((JSONObject) ((JSONObject) p.getPlayerObject().get("stats")).get("skywars")).get("ranked"));

            EmbedBuilder pageOne = new EmbedBuilder();
            pageOne.setTitle(playerName + "'s Ranked Positions");
            pageOne.setColor(p.getRank().getColor());
            pageOne.setThumbnail("https://hypixel.net/styles/hypixel-v2/images/game-icons/Skywars-64.png");

            EmbedBuilder pageTwo = new EmbedBuilder();
            pageTwo.setTitle(playerName + "'s Ranked Positions");
            pageTwo.setColor(p.getRank().getColor());
            pageTwo.setThumbnail("https://hypixel.net/styles/hypixel-v2/images/game-icons/Skywars-64.png");

            EmbedBuilder pageThree = new EmbedBuilder();
            pageThree.setTitle(playerName + "'s Ranked Positions");
            pageThree.setColor(p.getRank().getColor());
            pageThree.setThumbnail("https://hypixel.net/styles/hypixel-v2/images/game-icons/Skywars-64.png");

            int totalRating = 0;
            int totalPos = 0;
            int count = 0;

            String highestRating;
            String highestPos;


            List<MessageEmbed.Field> pageOneSeasonFields = new ArrayList<>();
            List<MessageEmbed.Field> pageTwoSeasonFields = new ArrayList<>();
            List<MessageEmbed.Field> pageThreeSeasonFields = new ArrayList<>();

            Map.Entry<Integer, Integer> maxRatingEntry = null;
            Map.Entry<Integer, Integer> maxPositionEntry = null;
            TreeMap<Integer, Integer> tempRatingEntry = new TreeMap<>();
            TreeMap<Integer, Integer> tempPositionEntry = new TreeMap<>();


            for (Map.Entry<Integer, int[]> entry : rankedSeasons.entrySet()) {
                tempRatingEntry.clear();
                tempPositionEntry.clear();
                tempRatingEntry.put(entry.getValue()[0], entry.getValue()[1] + 1);
                tempPositionEntry.put(entry.getValue()[0], entry.getValue()[1] + 1);

                if (maxRatingEntry == null || entry.getValue()[0] - maxRatingEntry.getKey() > 0)
                    maxRatingEntry = tempRatingEntry.firstEntry();
                if (maxPositionEntry == null || ((entry.getValue()[1] + 1) - maxPositionEntry.getValue() < 0))
                    maxPositionEntry = tempPositionEntry.firstEntry();


                count++;
                totalRating += entry.getValue()[0];
                totalPos += entry.getValue()[1] + 1;

                MessageEmbed.Field embedField = new MessageEmbed.Field("Season " + entry.getKey(), entry.getValue()[0] + " #" + entry.getValue()[1] + 1, true);


                if (count < 20) {
                    pageOneSeasonFields.add(embedField);
                } else if (count < 45) {
                    pageTwoSeasonFields.add(embedField);
                } else {
                    pageThreeSeasonFields.add(embedField);
                }
            }

            if (pageOneSeasonFields.size() == 0)
                return new MessageEmbed[]{pageOne.addField("Didn't participate in any seasons", "*error*", true).build()};

            highestRating = maxRatingEntry.getKey() + " #" + maxRatingEntry.getValue();
            highestPos = maxPositionEntry.getKey() + " #" + maxPositionEntry.getValue();


            pageOne.addField("Highest Rating", highestRating, true);
            pageOne.addField("Lowest Position", highestPos, true);
            pageOne.addField("Averages", totalRating / count + " #" + totalPos / count, true);
            pageOneSeasonFields.forEach(pageOne::addField);
            pageTwoSeasonFields.forEach(pageTwo::addField);
            pageThreeSeasonFields.forEach(pageThree::addField);

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            pageOne.setFooter("Took " + duration + "ms");
            pageTwo.setFooter("Took " + duration + "ms");
            pageThree.setFooter("Took " + duration + "ms");


            if (pageTwoSeasonFields.size() > 0) {
                if (pageThreeSeasonFields.size() > 0) {
                    return new MessageEmbed[]{pageOne.build(), pageTwo.build(), pageThree.build()};
                } else {
                    return new MessageEmbed[]{pageOne.build(), pageTwo.build()};
                }
            } else {
                return new MessageEmbed[]{pageOne.build()};
            }

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
                                MessageEmbed[] embeds = getStatsEmbed(playerName);
                                assert embeds != null;
                                MessageEmbed statsEmbed = embeds[0];
                                if (statsEmbed.equals(Utils.invalidUsernameEmbed())) {
                                    msg.editMessage(statsEmbed).queue();
                                    return;
                                }
                                msg.editMessage(statsEmbed).queue();

                                PageHandler.managePages(waiter, event.getAuthor(), msg, embeds, 0, true);
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
                MessageEmbed[] embeds = getStatsEmbed(playerName);
                assert embeds != null;
                MessageEmbed statsEmbed = embeds[0];
                if (statsEmbed.equals(Utils.invalidUsernameEmbed())) {
                    msg.editMessage(statsEmbed).queue();
                    return;
                }

                msg.editMessage(statsEmbed).queue();

                PageHandler.managePages(waiter, event.getAuthor(), msg, embeds, 0, true);
            });

        }
    }
}
