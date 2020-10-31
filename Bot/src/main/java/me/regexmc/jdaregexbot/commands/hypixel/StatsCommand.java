package me.regexmc.jdaregexbot.commands.hypixel;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.regexmc.jdaregexbot.util.Cache;
import me.regexmc.jdaregexbot.util.Player;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class StatsCommand extends Command {
    private static long startTime;
    private final EventWaiter waiter;

    public StatsCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "stats";
        this.help = "Get player stats";
        this.cooldown = 10;
    }

    private static MessageEmbed getStatsEmbed(String playerName) {
        try {
            String UUID = Cache.getUUIDFromCache(playerName);
            if (UUID.equals("Invalid Username")) {
                return Utils.invalidUsernameEmbed();
            }

            Player p = Cache.handle(UUID);
            if (p == null) return Utils.invalidUsernameEmbed();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(playerName + "'s General Stats");
            embedBuilder.setColor(p.getRank().getColor());
            embedBuilder.addField("Rank", p.getRank().getChat(), false);
            embedBuilder.addField("Level", Double.toString(p.getNetworkLevel()), false);
            embedBuilder.addField("Karma", p.getKarma(), false);
            embedBuilder.addField("**Estimated** Achievement Points (w/ Legacy)", Integer.toString(p.getAchievementPoints()), false);
            embedBuilder.setThumbnail("https://crafatar.com/avatars/" + UUID + ".png");

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            embedBuilder.setFooter("Took " + duration + "ms");
            return embedBuilder.build();
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
                                MessageEmbed statsEmbed = getStatsEmbed(playerName);
                                assert statsEmbed != null;
                                msg.editMessage(statsEmbed).queue();
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
                MessageEmbed statsEmbed = getStatsEmbed(playerName);
                assert statsEmbed != null;
                msg.editMessage(statsEmbed).queue();
            });

        }
    }
}