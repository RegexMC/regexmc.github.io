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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SkywarsCommand extends Command {
    private static long startTime;
    private final EventWaiter waiter;

    public SkywarsCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "skywars";
        this.help = "Get player skywars stats";
        this.cooldown = 10;
    }

    private static MessageEmbed[] getStatsEmbeds(String playerName) {
        try {
            String UUID = Cache.getUUIDFromCache(playerName);
            if (UUID.equals("Invalid Username")) {
                return new MessageEmbed[]{Utils.invalidUsernameEmbed()};
            }

            Player p = Cache.handle(UUID);
            if (p == null) return new MessageEmbed[]{Utils.invalidUsernameEmbed()};

            //region Skywars Objects
            JSONObject skywarsObject = p.getPlayerObject().getJSONObject("stats").getJSONObject("skywars");
            JSONObject soloNormalObject = skywarsObject.getJSONObject("solo_normal");
            JSONObject soloInsaneObject = skywarsObject.getJSONObject("solo_insane");
            JSONObject teamsNormalObject = skywarsObject.getJSONObject("teams_normal");
            JSONObject teamsInsaneObject = skywarsObject.getJSONObject("teams_insane");
            //JSONObject rankedObject = skywarsObject.getJSONObject("ranked");
            //endregion


            //region Overall Stats
            int exp = skywarsObject.getInt("experience");
            double level = Utils.round(skywarsObject.getDouble("level"), 3);

            int kills = skywarsObject.getInt("kills");
            int deaths = skywarsObject.getInt("deaths");
            int wins = skywarsObject.getInt("wins");
            int losses = skywarsObject.getInt("losses");

            int heads = skywarsObject.getInt("heads");
            int shards = skywarsObject.getInt("shards");
            int opals = skywarsObject.getInt("opals");

            long playtime = Long.parseLong(skywarsObject.get("time_played").toString());

            double KD = Utils.round((double) kills / deaths, 3);
            double WL = Utils.round((double) wins / losses, 3);
            //endregion

            //region Solo Normal Stats
            int soloNormalKills = soloNormalObject.getInt("kills");
            int soloNormalDeaths = soloNormalObject.getInt("deaths");
            double soloNormalKD = Utils.round((double) soloNormalKills / soloNormalDeaths, 3);

            int soloNormalWins = soloNormalObject.getInt("wins");
            int soloNormalLosses = soloNormalObject.getInt("losses");
            double soloNormalWL = Utils.round((double) soloNormalWins / soloNormalLosses, 3);
            //endregion

            //region Solo Insane Stats
            int soloInsaneKills = soloInsaneObject.getInt("kills");
            int soloInsaneDeaths = soloInsaneObject.getInt("deaths");
            double soloInsaneKD = Utils.round((double) soloInsaneKills / soloInsaneDeaths, 3);

            int soloInsaneWins = soloInsaneObject.getInt("wins");
            int soloInsaneLosses = soloInsaneObject.getInt("losses");
            double soloInsaneWL = Utils.round((double) soloInsaneWins / soloInsaneLosses, 3);
            //endregion

            //region Teams Normal Stats
            int teamsNormalKills = teamsNormalObject.getInt("kills");
            int teamsNormalDeaths = teamsNormalObject.getInt("deaths");
            double teamsNormalKD = Utils.round((double) teamsNormalKills / teamsNormalDeaths, 3);

            int teamsNormalWins = teamsNormalObject.getInt("wins");
            int teamsNormalLosses = teamsNormalObject.getInt("losses");
            double teamsNormalWL = Utils.round((double) teamsNormalWins / teamsNormalLosses, 3);
            //endregion

            //region Teams Insane Stats
            int teamsInsaneKills = teamsInsaneObject.getInt("kills");
            int teamsInsaneDeaths = teamsInsaneObject.getInt("deaths");
            double teamsInsaneKD = Utils.round((double) teamsInsaneKills / teamsInsaneDeaths, 3);

            int teamsInsaneWins = teamsInsaneObject.getInt("wins");
            int teamsInsaneLosses = teamsInsaneObject.getInt("losses");
            double teamsInsaneWL = Utils.round((double) teamsInsaneWins / teamsInsaneLosses, 3);
            //endregion


            //region Overall Embed
            EmbedBuilder overallEmbed = new EmbedBuilder();
            overallEmbed.setTitle(playerName + "'s Overall Skywars Stats");
            overallEmbed.setColor(p.getRank().getColor());
            overallEmbed.addField("Experience", String.valueOf(exp), true);
            overallEmbed.addField("Level", String.valueOf(level), true);
            overallEmbed.addBlankField(true);
            overallEmbed.addField("Kills", String.valueOf(kills), true);
            overallEmbed.addField("Deaths", String.valueOf(deaths), true);
            overallEmbed.addField("K/D", String.valueOf(KD), true);
            overallEmbed.addField("Wins", String.valueOf(wins), true);
            overallEmbed.addField("Losses", String.valueOf(losses), true);
            overallEmbed.addField("W/L", String.valueOf(WL), true);
            overallEmbed.addField("Heads", String.valueOf(heads), true);
            overallEmbed.addField("Shards", String.valueOf(shards), true);
            overallEmbed.addField("Opals", String.valueOf(opals), true);
            overallEmbed.addField("Playtime (DD:HH:MM)", Utils.timeConvert(playtime / 60, "default"), true);
            overallEmbed.setThumbnail("https://hypixel.net/styles/hypixel-v2/images/game-icons/Skywars-64.png");
            //endregion

            //region Solo Normal Embed
            EmbedBuilder soloNormalEmbed = new EmbedBuilder();
            soloNormalEmbed.setTitle(playerName + "'s Solo Normal Skywars Stats");
            soloNormalEmbed.setColor(p.getRank().getColor());
            soloNormalEmbed.addField("Kills", String.valueOf(soloNormalKills), true);
            soloNormalEmbed.addField("Deaths", String.valueOf(soloNormalDeaths), true);
            soloNormalEmbed.addField("K/D", String.valueOf(soloNormalKD), true);
            soloNormalEmbed.addField("Wins", String.valueOf(soloNormalWins), true);
            soloNormalEmbed.addField("Losses", String.valueOf(soloNormalLosses), true);
            soloNormalEmbed.addField("W/L", String.valueOf(soloNormalWL), true);
            //endregion

            //region Solo Insane Embed
            EmbedBuilder soloInsaneEmbed = new EmbedBuilder();
            soloInsaneEmbed.setTitle(playerName + "'s Solo Insane Skywars Stats");
            soloInsaneEmbed.setColor(p.getRank().getColor());
            soloInsaneEmbed.addField("Kills", String.valueOf(soloInsaneKills), true);
            soloInsaneEmbed.addField("Deaths", String.valueOf(soloInsaneDeaths), true);
            soloInsaneEmbed.addField("K/D", String.valueOf(soloInsaneKD), true);
            soloInsaneEmbed.addField("Wins", String.valueOf(soloInsaneWins), true);
            soloInsaneEmbed.addField("Losses", String.valueOf(soloInsaneLosses), true);
            soloInsaneEmbed.addField("W/L", String.valueOf(soloInsaneWL), true);
            //endregion

            //region Teams Normal Embed
            EmbedBuilder teamsNormalEmbed = new EmbedBuilder();
            teamsNormalEmbed.setTitle(playerName + "'s Teams Normal Skywars Stats");
            teamsNormalEmbed.setColor(p.getRank().getColor());
            teamsNormalEmbed.addField("Kills", String.valueOf(teamsNormalKills), true);
            teamsNormalEmbed.addField("Deaths", String.valueOf(teamsNormalDeaths), true);
            teamsNormalEmbed.addField("K/D", String.valueOf(teamsNormalKD), true);
            teamsNormalEmbed.addField("Wins", String.valueOf(teamsNormalWins), true);
            teamsNormalEmbed.addField("Losses", String.valueOf(teamsNormalLosses), true);
            teamsNormalEmbed.addField("W/L", String.valueOf(teamsNormalWL), true);
            //endregion

            //region Teams Insane Embed
            EmbedBuilder teamsInsaneEmbed = new EmbedBuilder();
            teamsInsaneEmbed.setTitle(playerName + "'s Teams Insane Skywars Stats");
            teamsInsaneEmbed.setColor(p.getRank().getColor());
            teamsInsaneEmbed.addField("Kills", String.valueOf(teamsInsaneKills), true);
            teamsInsaneEmbed.addField("Deaths", String.valueOf(teamsInsaneDeaths), true);
            teamsInsaneEmbed.addField("K/D", String.valueOf(teamsInsaneKD), true);
            teamsInsaneEmbed.addField("Wins", String.valueOf(teamsInsaneWins), true);
            teamsInsaneEmbed.addField("Losses", String.valueOf(teamsInsaneLosses), true);
            teamsInsaneEmbed.addField("W/L", String.valueOf(teamsInsaneWL), true);
            //endregion


            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            overallEmbed.setFooter("Took " + duration + "ms");
            soloNormalEmbed.setFooter("Took " + duration + "ms");
            soloInsaneEmbed.setFooter("Took " + duration + "ms");
            teamsNormalEmbed.setFooter("Took " + duration + "ms");
            teamsInsaneEmbed.setFooter("Took " + duration + "ms");

            return new MessageEmbed[]{overallEmbed.build(), soloNormalEmbed.build(), soloInsaneEmbed.build(), teamsNormalEmbed.build(), teamsInsaneEmbed.build()};
        } catch (IOException | InterruptedException | ExecutionException e) {
            Utils.log(e, Utils.ErrorTypes.ERROR);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            if (event.getMessage().getContentRaw().split(" ", 2).length < 2) {
                event.reply("What is the players name?");

                waiter.waitForEvent(MessageReceivedEvent.class,
                        e -> Utils.messageReplyChecks(e, event),
                        e -> {
                            startTime = System.nanoTime();
                            String playerName = e.getMessage().getContentRaw();
                            if (Utils.invalidUsername(playerName)) {
                                event.reply(Utils.invalidUsernameEmbed());
                                return;
                            }

                            event.getChannel().sendMessage(Utils.gettingStatsEmbed()).queue(msg -> {
                                MessageEmbed[] embeds = getStatsEmbeds(playerName);
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

            startTime = System.nanoTime();

            String playerName = event.getMessage().getContentRaw().split(" ", 2)[1];
            if (Utils.invalidUsername(playerName)) {
                event.reply(Utils.invalidUsernameEmbed());
                return;
            }

            event.getChannel().sendMessage(Utils.gettingStatsEmbed()).queue(msg -> {
                MessageEmbed[] embeds = getStatsEmbeds(playerName);
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