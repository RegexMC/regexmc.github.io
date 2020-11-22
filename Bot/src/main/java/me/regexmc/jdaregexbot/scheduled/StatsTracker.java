package me.regexmc.jdaregexbot.scheduled;

import me.regexmc.jdaregexbot.BotMain;
import me.regexmc.jdaregexbot.util.RankUtils;
import me.regexmc.jdaregexbot.util.TrackerPlayerObject;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class StatsTracker extends TimerTask {
    @Override
    public void run() {
        try {
            JSONObject keysJSON = Utils.readJsonFromFile(BotMain.config.get("path_json") + "apikeys.json");

            Iterator<String> keys = keysJSON.keys();

            while (keys.hasNext()) {
                String key = keys.next();

                JSONObject playerInfo = keysJSON.getJSONObject(key);
                if (playerInfo.getString("channel").isEmpty()) return;

                String apiKey = playerInfo.getString("key");
                String discordID = playerInfo.getString("id");
                String channelToSendTo = playerInfo.getString("channel");

                String url = "https://api.hypixel.net/player?key=" + apiKey + "&uuid=" + key;
                JSONObject json;
                try {
                    json = Utils.readJsonFromUrl(url);
                } catch (IOException e) {
                    return;
                }

                TrackerPlayerObject newPlayerObject = new TrackerPlayerObject(json.getJSONObject("player"), new Date());

                ArrayList<EmbedBuilder> embeds = new ArrayList<>();
                EmbedBuilder connectEmbed = new EmbedBuilder();

                if (BotMain.oldTrackerPlayerObjectHashMap.containsKey(key)) {
                    TrackerPlayerObject oldPlayerObject = BotMain.oldTrackerPlayerObjectHashMap.get(key);

                    if (oldPlayerObject.getStatus().equals("Offline") && newPlayerObject.getStatus().equals("Online")) {
                        BotMain.oldTrackerPlayerObjectHashMap.replace(key, newPlayerObject);
                        //player logged on

                        connectEmbed.setTitle(newPlayerObject.getUsername() + " connected");
                        connectEmbed.addField("Started tracking stats", Utils.parseDate(new Date()), true);
                        connectEmbed.setColor(RankUtils.Ranks.VIP.getColor());
                        Objects.requireNonNull(BotMain.bot.getTextChannelById(channelToSendTo)).sendMessage(connectEmbed.build()).queue();

                    } else if (oldPlayerObject.getStatus().equals("Online") && newPlayerObject.getStatus().equals("Offline")) {
                        //player logged off

                        connectEmbed.setTitle(newPlayerObject.getUsername() + " disconnected");
                        Date now = new Date();
                        connectEmbed.setDescription("Stats for (<@" + discordID + ">)");
                        connectEmbed.addField("Stopped tracking stats", Utils.parseDate(oldPlayerObject.getSetDate()) + " - " + Utils.parseDate(now) + " **(" + Utils.timeConvert(Utils.getDateDiff(oldPlayerObject.getSetDate(), now, TimeUnit.SECONDS), "default") + ")**", false);
                        connectEmbed.setColor(RankUtils.Ranks.ADMIN.getColor());
                        embeds.add(connectEmbed);

                        JSONObject statsObject = newPlayerObject.getStats();
                        JSONObject oldStatsObject = oldPlayerObject.getStats();

                        JSONObject duelsObject = statsObject.getJSONObject("Duels");
                        JSONObject oldDuelsObject = oldStatsObject.getJSONObject("Duels");

                        JSONObject bedwarsObject = statsObject.getJSONObject("Bedwars");
                        JSONObject oldBedwarsObject = oldStatsObject.getJSONObject("Bedwars");

                        EmbedBuilder duelsStatsEmbed = new EmbedBuilder();
                        EmbedBuilder bedwarsStatsEmbed = new EmbedBuilder();

                        //region Overall Duels
                        int oldWins = oldDuelsObject.optInt("wins");
                        int oldLosses = oldDuelsObject.optInt("losses");
                        double oldWL = Utils.round(oldWins / (double) oldLosses, 3);
                        int wins = duelsObject.optInt("wins");
                        int losses = duelsObject.optInt("losses");
                        double WL = Utils.round(wins / (double) losses, 3);

                        int oldKills = oldDuelsObject.optInt("kills");
                        int oldDeaths = oldDuelsObject.optInt("deaths");
                        double oldKD = Utils.round(oldKills / (double) oldDeaths, 3);
                        int kills = oldDuelsObject.optInt("kills");
                        int deaths = oldDuelsObject.optInt("deaths");
                        double KD = Utils.round(kills / (double) deaths, 3);

                        int winsDifference = wins - oldWins;
                        int lossesDifference = losses - oldLosses;
                        double WLDifference = Utils.round(WL - oldWL, 3);
                        int killsDifference = kills - oldKills;
                        int deathsDifference = deaths - oldDeaths;
                        double KDDifference = Utils.round(KD - oldKD, 3);

                        StringBuilder changesFieldValue = new StringBuilder().append("`");
                        boolean showChangesField = winsDifference != 0 || lossesDifference != 0 || WLDifference != 0.0 || killsDifference != 0 || deathsDifference != 0 || KDDifference != 0;

                        changesFieldValue.append("\u2022Wins: ").append(wins).append(" (").append(winsDifference > 0 ? "+" + winsDifference : winsDifference).append(")\n");
                        changesFieldValue.append("\u2022Losses: ").append(losses).append(" (").append(lossesDifference > 0 ? "+" + lossesDifference : lossesDifference).append(")\n");
                        changesFieldValue.append("\u2022W/L: ").append(WL).append(" (").append(WLDifference > 0 ? "+" + WLDifference : WLDifference).append(")\n");
                        changesFieldValue.append("\u2022Kills: ").append(kills).append(" (").append(killsDifference > 0 ? "+" + killsDifference : killsDifference).append(")\n");
                        changesFieldValue.append("\u2022Deaths: ").append(deaths).append(" (").append(deathsDifference > 0 ? "+" + deathsDifference : deathsDifference).append(")\n");
                        changesFieldValue.append("\u2022K/D: ").append(KD).append(" (").append(KDDifference > 0 ? "+" + KDDifference : KDDifference).append(")\n");

                        changesFieldValue.append("`");

                        if (showChangesField)
                            duelsStatsEmbed.addField("Overall Duels", changesFieldValue.toString(), false);
                        //endregion

                        //region Skywars Duels
                        changesFieldValue = new StringBuilder().append("`");

                        int skywarsOldWins = oldDuelsObject.optInt("sw_duel_wins");
                        int skywarsOldLosses = oldDuelsObject.optInt("sw_duel_losses");
                        double skywarsOldWL = Utils.round(skywarsOldWins / (double) skywarsOldLosses, 3);
                        int skywarsOldKills = oldDuelsObject.optInt("sw_duel_kills");
                        int skywarsOldDeaths = oldDuelsObject.optInt("sw_duel_deaths");
                        double skywarsOldKD = Utils.round(skywarsOldKills / (double) skywarsOldDeaths, 3);

                        int skywarsWins = duelsObject.optInt("sw_duel_wins");
                        int skywarsLosses = duelsObject.optInt("sw_duel_losses");
                        double skywarsWL = Utils.round(skywarsWins / (double) skywarsLosses, 3);
                        int skywarsKills = duelsObject.optInt("sw_duel_kills");
                        int skywarsDeaths = duelsObject.optInt("sw_duel_deaths");
                        double skywarsKD = Utils.round(skywarsKills / (double) skywarsDeaths, 3);

                        int skywarsWinsDifference = skywarsWins - skywarsOldWins;
                        int skywarsLossesDifference = skywarsLosses - skywarsOldLosses;
                        double skywarsWLDifference = Utils.round(skywarsWL - skywarsOldWL, 3);
                        int skywarsKillsDifference = skywarsKills - skywarsOldKills;
                        int skywarsDeathsDifference = skywarsDeaths - skywarsOldDeaths;
                        double skywarsKDDifference = Utils.round(skywarsKD - skywarsOldKD, 3);

                        showChangesField = skywarsWinsDifference != 0 || skywarsLossesDifference != 0 || skywarsWLDifference != 0.0 || skywarsKillsDifference != 0 || skywarsDeathsDifference != 0 || skywarsKDDifference != 0;

                        changesFieldValue.append("\u2022Wins: ").append(skywarsWins).append(" (").append(skywarsWinsDifference > 0 ? "+" + skywarsWinsDifference : skywarsWinsDifference).append(")\n");
                        changesFieldValue.append("\u2022Losses: ").append(skywarsLosses).append(" (").append(skywarsLossesDifference > 0 ? "+" + skywarsLossesDifference : skywarsLossesDifference).append(")\n");
                        changesFieldValue.append("\u2022W/L: ").append(skywarsWL).append(" (").append(skywarsWLDifference > 0 ? "+" + skywarsWLDifference : skywarsWLDifference).append(")\n");
                        changesFieldValue.append("\u2022Kills: ").append(skywarsKills).append(" (").append(skywarsKillsDifference > 0 ? "+" + skywarsKillsDifference : skywarsKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Deaths: ").append(skywarsDeaths).append(" (").append(skywarsDeathsDifference > 0 ? "+" + skywarsDeathsDifference : skywarsDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022K/D: ").append(skywarsKD).append(" (").append(skywarsKDDifference > 0 ? "+" + skywarsKDDifference : skywarsKDDifference).append(")\n");

                        changesFieldValue.append("`");

                        if (showChangesField)
                            duelsStatsEmbed.addField("Skywars Duels", changesFieldValue.toString(), false);
                        //endregion

                        //region Sumo Duels
                        changesFieldValue = new StringBuilder().append("`");

                        int sumoOldWins = oldDuelsObject.optInt("sumo_duel_wins");
                        int sumoOldLosses = oldDuelsObject.optInt("sumo_duel_losses");
                        double sumoOldWL = Utils.round(sumoOldWins / (double) sumoOldLosses, 3);
                        int sumoOldKills = oldDuelsObject.optInt("sumo_duel_kills");
                        int sumoOldDeaths = oldDuelsObject.optInt("sumo_duel_deaths");
                        double sumoOldKD = Utils.round(sumoOldKills / (double) sumoOldDeaths, 3);

                        int sumoWins = duelsObject.optInt("sumo_duel_wins");
                        int sumoLosses = duelsObject.optInt("sumo_duel_losses");
                        double sumoWL = Utils.round(sumoWins / (double) sumoLosses, 3);
                        int sumoKills = duelsObject.optInt("sumo_duel_kills");
                        int sumoDeaths = duelsObject.optInt("sumo_duel_deaths");
                        double sumoKD = Utils.round(sumoKills / (double) sumoDeaths, 3);

                        int sumoWinsDifference = sumoWins - sumoOldWins;
                        int sumoLossesDifference = sumoLosses - sumoOldLosses;
                        double sumoWLDifference = Utils.round(sumoWL - sumoOldWL, 3);
                        int sumoKillsDifference = sumoKills - sumoOldKills;
                        int sumoDeathsDifference = sumoDeaths - sumoOldDeaths;
                        double sumoKDDifference = Utils.round(sumoKD - sumoOldKD, 3);

                        showChangesField = sumoWinsDifference != 0 || sumoLossesDifference != 0 || sumoWLDifference != 0.0 || sumoKillsDifference != 0 || sumoDeathsDifference != 0 || sumoKDDifference != 0;

                        changesFieldValue.append("\u2022Wins: ").append(sumoWins).append(" (").append(sumoWinsDifference > 0 ? "+" + sumoWinsDifference : sumoWinsDifference).append(")\n");
                        changesFieldValue.append("\u2022Losses: ").append(sumoLosses).append(" (").append(sumoLossesDifference > 0 ? "+" + sumoLossesDifference : sumoLossesDifference).append(")\n");
                        changesFieldValue.append("\u2022W/L: ").append(sumoWL).append(" (").append(sumoWLDifference > 0 ? "+" + sumoWLDifference : sumoWLDifference).append(")\n");
                        changesFieldValue.append("\u2022Kills: ").append(sumoKills).append(" (").append(sumoKillsDifference > 0 ? "+" + sumoKillsDifference : sumoKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Deaths: ").append(sumoDeaths).append(" (").append(sumoDeathsDifference > 0 ? "+" + sumoDeathsDifference : sumoDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022K/D: ").append(sumoKD).append(" (").append(sumoKDDifference > 0 ? "+" + sumoKDDifference : sumoKDDifference).append(")\n");

                        changesFieldValue.append("`");

                        if (showChangesField)
                            duelsStatsEmbed.addField("Sumo Duels", changesFieldValue.toString(), false);

                        //endregion

                        //region UHC Duels
                        changesFieldValue = new StringBuilder().append("`");

                        int uhcOldWins = oldDuelsObject.optInt("uhc_duel_wins");
                        int uhcOldLosses = oldDuelsObject.optInt("uhc_duel_losses");
                        double uhcOldWL = Utils.round(uhcOldWins / (double) uhcOldLosses, 3);
                        int uhcOldKills = oldDuelsObject.optInt("uhc_duel_kills");
                        int uhcOldDeaths = oldDuelsObject.optInt("uhc_duel_deaths");
                        double uhcOldKD = Utils.round(uhcOldKills / (double) uhcOldDeaths, 3);

                        int uhcWins = duelsObject.optInt("uhc_duel_wins");
                        int uhcLosses = duelsObject.optInt("uhc_duel_losses");
                        double uhcWL = Utils.round(uhcWins / (double) uhcLosses, 3);
                        int uhcKills = duelsObject.optInt("uhc_duel_kills");
                        int uhcDeaths = duelsObject.optInt("uhc_duel_deaths");
                        double uhcKD = Utils.round(uhcKills / (double) uhcDeaths, 3);

                        int uhcWinsDifference = uhcWins - uhcOldWins;
                        int uhcLossesDifference = uhcLosses - uhcOldLosses;
                        double uhcWLDifference = Utils.round(uhcWL - uhcOldWL, 3);
                        int uhcKillsDifference = uhcKills - uhcOldKills;
                        int uhcDeathsDifference = uhcDeaths - uhcOldDeaths;
                        double uhcKDDifference = Utils.round(uhcKD - uhcOldKD, 3);

                        showChangesField = uhcWinsDifference != 0 || uhcLossesDifference != 0 || uhcWLDifference != 0.0 || uhcKillsDifference != 0 || uhcDeathsDifference != 0 || uhcKDDifference != 0;

                        changesFieldValue.append("\u2022Wins: ").append(uhcWins).append(" (").append(uhcWinsDifference > 0 ? "+" + uhcWinsDifference : uhcWinsDifference).append(")\n");
                        changesFieldValue.append("\u2022Losses: ").append(uhcLosses).append(" (").append(uhcLossesDifference > 0 ? "+" + uhcLossesDifference : uhcLossesDifference).append(")\n");
                        changesFieldValue.append("\u2022W/L: ").append(uhcWL).append(" (").append(uhcWLDifference > 0 ? "+" + uhcWLDifference : uhcWLDifference).append(")\n");
                        changesFieldValue.append("\u2022Kills: ").append(uhcKills).append(" (").append(uhcKillsDifference > 0 ? "+" + uhcKillsDifference : uhcKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Deaths: ").append(uhcDeaths).append(" (").append(uhcDeathsDifference > 0 ? "+" + uhcDeathsDifference : uhcDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022K/D: ").append(uhcKD).append(" (").append(uhcKDDifference > 0 ? "+" + uhcKDDifference : uhcKDDifference).append(")\n");

                        changesFieldValue.append("`");

                        if (showChangesField)
                            duelsStatsEmbed.addField("UHC Duels", changesFieldValue.toString(), false);

                        if (duelsStatsEmbed.getFields().size() > 0) embeds.add(duelsStatsEmbed);

                        //endregion

                        //region Overall Bedwars
                        changesFieldValue = new StringBuilder().append("`");

                        int bedwarsOverallOldWinstreak = oldBedwarsObject.optInt("winstreak");
                        int bedwarsOverallOldWins = oldBedwarsObject.optInt("wins_bedwars");
                        int bedwarsOverallOldLosses = oldBedwarsObject.optInt("losses_bedwars");
                        double bedwarsOverallOldWL = Utils.round(bedwarsOverallOldWins / (double) bedwarsOverallOldLosses, 3);
                        int bedwarsOverallOldKills = oldBedwarsObject.optInt("kills_bedwars");
                        int bedwarsOverallOldDeaths = oldBedwarsObject.optInt("deaths_bedwars");
                        double bedwarsOverallOldKD = Utils.round(bedwarsOverallOldKills / (double) bedwarsOverallOldDeaths, 3);
                        int bedwarsOverallOldFinalKills = oldBedwarsObject.optInt("final_kills_bedwars");
                        int bedwarsOverallOldFinalDeaths = oldBedwarsObject.optInt("final_deaths_bedwars");
                        double bedwarsOverallOldFKD = Utils.round(bedwarsOverallOldFinalKills / (double) bedwarsOverallOldFinalDeaths, 3);

                        int bedwarsOverallWinstreak = bedwarsObject.optInt("winstreak");
                        int bedwarsOverallWins = bedwarsObject.optInt("wins_bedwars");
                        int bedwarsOverallLosses = bedwarsObject.optInt("losses_bedwars");
                        double bedwarsOverallWL = Utils.round(bedwarsOverallWins / (double) bedwarsOverallLosses, 3);
                        int bedwarsOverallKills = bedwarsObject.optInt("kills_bedwars");
                        int bedwarsOverallDeaths = bedwarsObject.optInt("deaths_bedwars");
                        double bedwarsOverallKD = Utils.round(bedwarsOverallKills / (double) bedwarsOverallDeaths, 3);
                        int bedwarsOverallFinalKills = bedwarsObject.optInt("final_kills_bedwars");
                        int bedwarsOverallFinalDeaths = bedwarsObject.optInt("final_deaths_bedwars");
                        double bedwarsOverallFKD = Utils.round(bedwarsOverallFinalKills / (double) bedwarsOverallFinalDeaths, 3);

                        int bedwarsOverallWinstreakDifference = bedwarsOverallWinstreak - bedwarsOverallOldWinstreak;
                        int bedwarsOverallWinsDifference = bedwarsOverallWins - bedwarsOverallOldWins;
                        int bedwarsOverallLossesDifference = bedwarsOverallLosses - bedwarsOverallOldLosses;
                        double bedwarsOverallWLDifference = Utils.round(bedwarsOverallWL - bedwarsOverallOldWL, 3);
                        int bedwarsOverallKillsDifference = bedwarsOverallKills - bedwarsOverallOldKills;
                        int bedwarsOverallDeathsDifference = bedwarsOverallDeaths - bedwarsOverallOldDeaths;
                        double bedwarsOverallKDDifference = Utils.round(bedwarsOverallKD - bedwarsOverallOldKD, 3);
                        int bedwarsOverallFinalKillsDifference = bedwarsOverallFinalKills - bedwarsOverallOldFinalKills;
                        int bedwarsOverallFinalDeathsDifference = bedwarsOverallFinalDeaths - bedwarsOverallOldFinalDeaths;
                        double bedwarsOverallFKDDifference = Utils.round(bedwarsOverallFKD - bedwarsOverallOldFKD, 3);

                        showChangesField = bedwarsOverallWinsDifference != 0 || bedwarsOverallLossesDifference != 0 || bedwarsOverallWLDifference != 0.0 || bedwarsOverallKillsDifference != 0 || bedwarsOverallDeathsDifference != 0 || bedwarsOverallKDDifference != 0;

                        changesFieldValue.append("\u2022Wins: ").append(bedwarsOverallWins).append(" (").append(bedwarsOverallWinsDifference > 0 ? "+" + bedwarsOverallWinsDifference : bedwarsOverallWinsDifference).append(")\n");
                        changesFieldValue.append("\u2022Losses: ").append(bedwarsOverallLosses).append(" (").append(bedwarsOverallLossesDifference > 0 ? "+" + bedwarsOverallLossesDifference : bedwarsOverallLossesDifference).append(")\n");
                        changesFieldValue.append("\u2022W/L: ").append(bedwarsOverallWL).append(" (").append(bedwarsOverallWLDifference > 0 ? "+" + bedwarsOverallWLDifference : bedwarsOverallWLDifference).append(")\n");
                        changesFieldValue.append("\u2022Kills: ").append(bedwarsOverallKills).append(" (").append(bedwarsOverallKillsDifference > 0 ? "+" + bedwarsOverallKillsDifference : bedwarsOverallKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Deaths: ").append(bedwarsOverallDeaths).append(" (").append(bedwarsOverallDeathsDifference > 0 ? "+" + bedwarsOverallDeathsDifference : bedwarsOverallDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022K/D: ").append(bedwarsOverallKD).append(" (").append(bedwarsOverallKDDifference > 0 ? "+" + bedwarsOverallKDDifference : bedwarsOverallKDDifference).append(")\n");
                        changesFieldValue.append("\u2022Final Kills: ").append(bedwarsOverallFinalKills).append(" (").append(bedwarsOverallFinalKillsDifference > 0 ? "+" + bedwarsOverallFinalKillsDifference : bedwarsOverallFinalKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Final Deaths: ").append(bedwarsOverallFinalDeaths).append(" (").append(bedwarsOverallFinalDeathsDifference > 0 ? "+" + bedwarsOverallFinalDeathsDifference : bedwarsOverallFinalDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022FK/D: ").append(bedwarsOverallFKD).append(" (").append(bedwarsOverallFKDDifference > 0 ? "+" + bedwarsOverallFKDDifference : bedwarsOverallFKDDifference).append(")\n");
                        changesFieldValue.append("\u2022Winstreak: ").append(bedwarsOverallWinstreak).append(" (").append(bedwarsOverallWinstreakDifference > 0 ? "+" + bedwarsOverallWinstreakDifference : bedwarsOverallWinstreakDifference).append(")\n");

                        changesFieldValue.append("`");

                        if (showChangesField)
                            bedwarsStatsEmbed.addField("Bedwars Overall", changesFieldValue.toString(), false);

                        //endregion

                        //region Solo Bedwars
                        changesFieldValue = new StringBuilder().append("`");

                        int bedwarsSoloOldWinstreak = oldBedwarsObject.optInt("eight_one_winstreak");
                        int bedwarsSoloOldWins = oldBedwarsObject.optInt("eight_one_wins_bedwars");
                        int bedwarsSoloOldLosses = oldBedwarsObject.optInt("eight_one_losses_bedwars");
                        double bedwarsSoloOldWL = Utils.round(bedwarsSoloOldWins / (double) bedwarsSoloOldLosses, 3);
                        int bedwarsSoloOldKills = oldBedwarsObject.optInt("eight_one_kills_bedwars");
                        int bedwarsSoloOldDeaths = oldBedwarsObject.optInt("eight_one_deaths_bedwars");
                        double bedwarsSoloOldKD = Utils.round(bedwarsSoloOldKills / (double) bedwarsSoloOldDeaths, 3);
                        int bedwarsSoloOldFinalKills = oldBedwarsObject.optInt("eight_one_final_kills_bedwars");
                        int bedwarsSoloOldFinalDeaths = oldBedwarsObject.optInt("eight_one_final_deaths_bedwars");
                        double bedwarsSoloOldFKD = Utils.round(bedwarsSoloOldFinalKills / (double) bedwarsSoloOldFinalDeaths, 3);

                        int bedwarsSoloWinstreak = bedwarsObject.optInt("eight_one_winstreak");
                        int bedwarsSoloWins = bedwarsObject.optInt("eight_one_wins_bedwars");
                        int bedwarsSoloLosses = bedwarsObject.optInt("eight_one_losses_bedwars");
                        double bedwarsSoloWL = Utils.round(bedwarsSoloWins / (double) bedwarsSoloLosses, 3);
                        int bedwarsSoloKills = bedwarsObject.optInt("eight_one_kills_bedwars");
                        int bedwarsSoloDeaths = bedwarsObject.optInt("eight_one_deaths_bedwars");
                        double bedwarsSoloKD = Utils.round(bedwarsSoloKills / (double) bedwarsSoloDeaths, 3);
                        int bedwarsSoloFinalKills = bedwarsObject.optInt("eight_one_final_kills_bedwars");
                        int bedwarsSoloFinalDeaths = bedwarsObject.optInt("eight_one_final_deaths_bedwars");
                        double bedwarsSoloFKD = Utils.round(bedwarsSoloFinalKills / (double) bedwarsSoloFinalDeaths, 3);

                        int bedwarsSoloWinstreakDifference = bedwarsSoloWinstreak - bedwarsSoloOldWinstreak;
                        int bedwarsSoloWinsDifference = bedwarsSoloWins - bedwarsSoloOldWins;
                        int bedwarsSoloLossesDifference = bedwarsSoloLosses - bedwarsSoloOldLosses;
                        double bedwarsSoloWLDifference = Utils.round(bedwarsSoloWL - bedwarsSoloOldWL, 3);
                        int bedwarsSoloKillsDifference = bedwarsSoloKills - bedwarsSoloOldKills;
                        int bedwarsSoloDeathsDifference = bedwarsSoloDeaths - bedwarsSoloOldDeaths;
                        double bedwarsSoloKDDifference = Utils.round(bedwarsSoloKD - bedwarsSoloOldKD, 3);
                        int bedwarsSoloFinalKillsDifference = bedwarsSoloFinalKills - bedwarsSoloOldFinalKills;
                        int bedwarsSoloFinalDeathsDifference = bedwarsSoloFinalDeaths - bedwarsSoloOldFinalDeaths;
                        double bedwarsSoloFKDDifference = Utils.round(bedwarsSoloFKD - bedwarsSoloOldFKD, 3);

                        showChangesField = bedwarsSoloWinsDifference != 0 || bedwarsSoloLossesDifference != 0 || bedwarsSoloWLDifference != 0.0 || bedwarsSoloKillsDifference != 0 || bedwarsSoloDeathsDifference != 0 || bedwarsSoloKDDifference != 0;

                        changesFieldValue.append("\u2022Wins: ").append(bedwarsSoloWins).append(" (").append(bedwarsSoloWinsDifference > 0 ? "+" + bedwarsSoloWinsDifference : bedwarsSoloWinsDifference).append(")\n");
                        changesFieldValue.append("\u2022Losses: ").append(bedwarsSoloLosses).append(" (").append(bedwarsSoloLossesDifference > 0 ? "+" + bedwarsSoloLossesDifference : bedwarsSoloLossesDifference).append(")\n");
                        changesFieldValue.append("\u2022W/L: ").append(bedwarsSoloWL).append(" (").append(bedwarsSoloWLDifference > 0 ? "+" + bedwarsSoloWLDifference : bedwarsSoloWLDifference).append(")\n");
                        changesFieldValue.append("\u2022Kills: ").append(bedwarsSoloKills).append(" (").append(bedwarsSoloKillsDifference > 0 ? "+" + bedwarsSoloKillsDifference : bedwarsSoloKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Deaths: ").append(bedwarsSoloDeaths).append(" (").append(bedwarsSoloDeathsDifference > 0 ? "+" + bedwarsSoloDeathsDifference : bedwarsSoloDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022K/D: ").append(bedwarsSoloKD).append(" (").append(bedwarsSoloKDDifference > 0 ? "+" + bedwarsSoloKDDifference : bedwarsSoloKDDifference).append(")\n");
                        changesFieldValue.append("\u2022Final Kills: ").append(bedwarsSoloFinalKills).append(" (").append(bedwarsSoloFinalKillsDifference > 0 ? "+" + bedwarsSoloFinalKillsDifference : bedwarsSoloFinalKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Final Deaths: ").append(bedwarsSoloFinalDeaths).append(" (").append(bedwarsSoloFinalDeathsDifference > 0 ? "+" + bedwarsSoloFinalDeathsDifference : bedwarsSoloFinalDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022FK/D: ").append(bedwarsSoloFKD).append(" (").append(bedwarsSoloFKDDifference > 0 ? "+" + bedwarsSoloFKDDifference : bedwarsSoloFKDDifference).append(")\n");
                        changesFieldValue.append("\u2022Winstreak: ").append(bedwarsSoloWinstreak).append(" (").append(bedwarsSoloWinstreakDifference > 0 ? "+" + bedwarsSoloWinstreakDifference : bedwarsSoloWinstreakDifference).append(")\n");

                        changesFieldValue.append("`");

                        if (showChangesField)
                            bedwarsStatsEmbed.addField("Bedwars Solo", changesFieldValue.toString(), false);

                        //endregion

                        //region Duos Bedwars
                        changesFieldValue = new StringBuilder().append("`");

                        int bedwarsDuosOldWinstreak = oldBedwarsObject.optInt("eight_two_winstreak");
                        int bedwarsDuosOldWins = oldBedwarsObject.optInt("eight_two_wins_bedwars");
                        int bedwarsDuosOldLosses = oldBedwarsObject.optInt("eight_two_losses_bedwars");
                        double bedwarsDuosOldWL = Utils.round(bedwarsDuosOldWins / (double) bedwarsDuosOldLosses, 3);
                        int bedwarsDuosOldKills = oldBedwarsObject.optInt("eight_two_kills_bedwars");
                        int bedwarsDuosOldDeaths = oldBedwarsObject.optInt("eight_two_deaths_bedwars");
                        double bedwarsDuosOldKD = Utils.round(bedwarsDuosOldKills / (double) bedwarsDuosOldDeaths, 3);
                        int bedwarsDuosOldFinalKills = oldBedwarsObject.optInt("eight_two_final_kills_bedwars");
                        int bedwarsDuosOldFinalDeaths = oldBedwarsObject.optInt("eight_two_final_deaths_bedwars");
                        double bedwarsDuosOldFKD = Utils.round(bedwarsDuosOldFinalKills / (double) bedwarsDuosOldFinalDeaths, 3);

                        int bedwarsDuosWinstreak = bedwarsObject.optInt("eight_two_winstreak");
                        int bedwarsDuosWins = bedwarsObject.optInt("eight_two_wins_bedwars");
                        int bedwarsDuosLosses = bedwarsObject.optInt("eight_two_losses_bedwars");
                        double bedwarsDuosWL = Utils.round(bedwarsDuosWins / (double) bedwarsDuosLosses, 3);
                        int bedwarsDuosKills = bedwarsObject.optInt("eight_two_kills_bedwars");
                        int bedwarsDuosDeaths = bedwarsObject.optInt("eight_two_deaths_bedwars");
                        double bedwarsDuosKD = Utils.round(bedwarsDuosKills / (double) bedwarsDuosDeaths, 3);
                        int bedwarsDuosFinalKills = bedwarsObject.optInt("eight_two_final_kills_bedwars");
                        int bedwarsDuosFinalDeaths = bedwarsObject.optInt("eight_two_final_deaths_bedwars");
                        double bedwarsDuosFKD = Utils.round(bedwarsDuosFinalKills / (double) bedwarsDuosFinalDeaths, 3);

                        int bedwarsDuosWinstreakDifference = bedwarsDuosWinstreak - bedwarsDuosOldWinstreak;
                        int bedwarsDuosWinsDifference = bedwarsDuosWins - bedwarsDuosOldWins;
                        int bedwarsDuosLossesDifference = bedwarsDuosLosses - bedwarsDuosOldLosses;
                        double bedwarsDuosWLDifference = Utils.round(bedwarsDuosWL - bedwarsDuosOldWL, 3);
                        int bedwarsDuosKillsDifference = bedwarsDuosKills - bedwarsDuosOldKills;
                        int bedwarsDuosDeathsDifference = bedwarsDuosDeaths - bedwarsDuosOldDeaths;
                        double bedwarsDuosKDDifference = Utils.round(bedwarsDuosKD - bedwarsDuosOldKD, 3);
                        int bedwarsDuosFinalKillsDifference = bedwarsDuosFinalKills - bedwarsDuosOldFinalKills;
                        int bedwarsDuosFinalDeathsDifference = bedwarsDuosFinalDeaths - bedwarsDuosOldFinalDeaths;
                        double bedwarsDuosFKDDifference = Utils.round(bedwarsDuosFKD - bedwarsDuosOldFKD, 3);

                        showChangesField = bedwarsDuosWinsDifference != 0 || bedwarsDuosLossesDifference != 0 || bedwarsDuosWLDifference != 0.0 || bedwarsDuosKillsDifference != 0 || bedwarsDuosDeathsDifference != 0 || bedwarsDuosKDDifference != 0;

                        changesFieldValue.append("\u2022Wins: ").append(bedwarsDuosWins).append(" (").append(bedwarsDuosWinsDifference > 0 ? "+" + bedwarsDuosWinsDifference : bedwarsDuosWinsDifference).append(")\n");
                        changesFieldValue.append("\u2022Losses: ").append(bedwarsDuosLosses).append(" (").append(bedwarsDuosLossesDifference > 0 ? "+" + bedwarsDuosLossesDifference : bedwarsDuosLossesDifference).append(")\n");
                        changesFieldValue.append("\u2022W/L: ").append(bedwarsDuosWL).append(" (").append(bedwarsDuosWLDifference > 0 ? "+" + bedwarsDuosWLDifference : bedwarsDuosWLDifference).append(")\n");
                        changesFieldValue.append("\u2022Kills: ").append(bedwarsDuosKills).append(" (").append(bedwarsDuosKillsDifference > 0 ? "+" + bedwarsDuosKillsDifference : bedwarsDuosKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Deaths: ").append(bedwarsDuosDeaths).append(" (").append(bedwarsDuosDeathsDifference > 0 ? "+" + bedwarsDuosDeathsDifference : bedwarsDuosDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022K/D: ").append(bedwarsDuosKD).append(" (").append(bedwarsDuosKDDifference > 0 ? "+" + bedwarsDuosKDDifference : bedwarsDuosKDDifference).append(")\n");
                        changesFieldValue.append("\u2022Final Kills: ").append(bedwarsDuosFinalKills).append(" (").append(bedwarsDuosFinalKillsDifference > 0 ? "+" + bedwarsDuosFinalKillsDifference : bedwarsDuosFinalKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Final Deaths: ").append(bedwarsDuosFinalDeaths).append(" (").append(bedwarsDuosFinalDeathsDifference > 0 ? "+" + bedwarsDuosFinalDeathsDifference : bedwarsDuosFinalDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022FK/D: ").append(bedwarsDuosFKD).append(" (").append(bedwarsDuosFKDDifference > 0 ? "+" + bedwarsDuosFKDDifference : bedwarsDuosFKDDifference).append(")\n");
                        changesFieldValue.append("\u2022Winstreak: ").append(bedwarsDuosWinstreak).append(" (").append(bedwarsDuosWinstreakDifference > 0 ? "+" + bedwarsDuosWinstreakDifference : bedwarsDuosWinstreakDifference).append(")\n");

                        changesFieldValue.append("`");

                        if (showChangesField)
                            bedwarsStatsEmbed.addField("Bedwars Duos", changesFieldValue.toString(), false);

                        //endregion

                        //region Threes Bedwars
                        changesFieldValue = new StringBuilder().append("`");

                        int bedwarsThreesOldWinstreak = oldBedwarsObject.optInt("four_three_winstreak");
                        int bedwarsThreesOldWins = oldBedwarsObject.optInt("four_three_wins_bedwars");
                        int bedwarsThreesOldLosses = oldBedwarsObject.optInt("four_three_losses_bedwars");
                        double bedwarsThreesOldWL = Utils.round(bedwarsThreesOldWins / (double) bedwarsThreesOldLosses, 3);
                        int bedwarsThreesOldKills = oldBedwarsObject.optInt("four_three_kills_bedwars");
                        int bedwarsThreesOldDeaths = oldBedwarsObject.optInt("four_three_deaths_bedwars");
                        double bedwarsThreesOldKD = Utils.round(bedwarsThreesOldKills / (double) bedwarsThreesOldDeaths, 3);
                        int bedwarsThreesOldFinalKills = oldBedwarsObject.optInt("four_three_final_kills_bedwars");
                        int bedwarsThreesOldFinalDeaths = oldBedwarsObject.optInt("four_three_final_deaths_bedwars");
                        double bedwarsThreesOldFKD = Utils.round(bedwarsThreesOldFinalKills / (double) bedwarsThreesOldFinalDeaths, 3);

                        int bedwarsThreesWinstreak = bedwarsObject.optInt("four_three_winstreak");
                        int bedwarsThreesWins = bedwarsObject.optInt("four_three_wins_bedwars");
                        int bedwarsThreesLosses = bedwarsObject.optInt("four_three_losses_bedwars");
                        double bedwarsThreesWL = Utils.round(bedwarsThreesWins / (double) bedwarsThreesLosses, 3);
                        int bedwarsThreesKills = bedwarsObject.optInt("four_three_kills_bedwars");
                        int bedwarsThreesDeaths = bedwarsObject.optInt("four_three_deaths_bedwars");
                        double bedwarsThreesKD = Utils.round(bedwarsThreesKills / (double) bedwarsThreesDeaths, 3);
                        int bedwarsThreesFinalKills = bedwarsObject.optInt("four_three_final_kills_bedwars");
                        int bedwarsThreesFinalDeaths = bedwarsObject.optInt("four_three_final_deaths_bedwars");
                        double bedwarsThreesFKD = Utils.round(bedwarsThreesFinalKills / (double) bedwarsThreesFinalDeaths, 3);

                        int bedwarsThreesWinstreakDifference = bedwarsThreesWinstreak - bedwarsThreesOldWinstreak;
                        int bedwarsThreesWinsDifference = bedwarsThreesWins - bedwarsThreesOldWins;
                        int bedwarsThreesLossesDifference = bedwarsThreesLosses - bedwarsThreesOldLosses;
                        double bedwarsThreesWLDifference = Utils.round(bedwarsThreesWL - bedwarsThreesOldWL, 3);
                        int bedwarsThreesKillsDifference = bedwarsThreesKills - bedwarsThreesOldKills;
                        int bedwarsThreesDeathsDifference = bedwarsThreesDeaths - bedwarsThreesOldDeaths;
                        double bedwarsThreesKDDifference = Utils.round(bedwarsThreesKD - bedwarsThreesOldKD, 3);
                        int bedwarsThreesFinalKillsDifference = bedwarsThreesFinalKills - bedwarsThreesOldFinalKills;
                        int bedwarsThreesFinalDeathsDifference = bedwarsThreesFinalDeaths - bedwarsThreesOldFinalDeaths;
                        double bedwarsThreesFKDDifference = Utils.round(bedwarsThreesFKD - bedwarsThreesOldFKD, 3);

                        showChangesField = bedwarsThreesWinsDifference != 0 || bedwarsThreesLossesDifference != 0 || bedwarsThreesWLDifference != 0.0 || bedwarsThreesKillsDifference != 0 || bedwarsThreesDeathsDifference != 0 || bedwarsThreesKDDifference != 0;

                        changesFieldValue.append("\u2022Wins: ").append(bedwarsThreesWins).append(" (").append(bedwarsThreesWinsDifference > 0 ? "+" + bedwarsThreesWinsDifference : bedwarsThreesWinsDifference).append(")\n");
                        changesFieldValue.append("\u2022Losses: ").append(bedwarsThreesLosses).append(" (").append(bedwarsThreesLossesDifference > 0 ? "+" + bedwarsThreesLossesDifference : bedwarsThreesLossesDifference).append(")\n");
                        changesFieldValue.append("\u2022W/L: ").append(bedwarsThreesWL).append(" (").append(bedwarsThreesWLDifference > 0 ? "+" + bedwarsThreesWLDifference : bedwarsThreesWLDifference).append(")\n");
                        changesFieldValue.append("\u2022Kills: ").append(bedwarsThreesKills).append(" (").append(bedwarsThreesKillsDifference > 0 ? "+" + bedwarsThreesKillsDifference : bedwarsThreesKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Deaths: ").append(bedwarsThreesDeaths).append(" (").append(bedwarsThreesDeathsDifference > 0 ? "+" + bedwarsThreesDeathsDifference : bedwarsThreesDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022K/D: ").append(bedwarsThreesKD).append(" (").append(bedwarsThreesKDDifference > 0 ? "+" + bedwarsThreesKDDifference : bedwarsThreesKDDifference).append(")\n");
                        changesFieldValue.append("\u2022Final Kills: ").append(bedwarsThreesFinalKills).append(" (").append(bedwarsThreesFinalKillsDifference > 0 ? "+" + bedwarsThreesFinalKillsDifference : bedwarsThreesFinalKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Final Deaths: ").append(bedwarsThreesFinalDeaths).append(" (").append(bedwarsThreesFinalDeathsDifference > 0 ? "+" + bedwarsThreesFinalDeathsDifference : bedwarsThreesFinalDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022FK/D: ").append(bedwarsThreesFKD).append(" (").append(bedwarsThreesFKDDifference > 0 ? "+" + bedwarsThreesFKDDifference : bedwarsThreesFKDDifference).append(")\n");
                        changesFieldValue.append("\u2022Winstreak: ").append(bedwarsThreesWinstreak).append(" (").append(bedwarsThreesWinstreakDifference > 0 ? "+" + bedwarsThreesWinstreakDifference : bedwarsThreesWinstreakDifference).append(")\n");

                        changesFieldValue.append("`");

                        if (showChangesField)
                            bedwarsStatsEmbed.addField("Bedwars Threes", changesFieldValue.toString(), false);

                        //endregion

                        //region Fours Bedwars
                        changesFieldValue = new StringBuilder().append("`");

                        int bedwarsFoursOldWinstreak = oldBedwarsObject.optInt("four_four_winstreak");
                        int bedwarsFoursOldWins = oldBedwarsObject.optInt("four_four_wins_bedwars");
                        int bedwarsFoursOldLosses = oldBedwarsObject.optInt("four_four_losses_bedwars");
                        double bedwarsFoursOldWL = Utils.round(bedwarsFoursOldWins / (double) bedwarsFoursOldLosses, 3);
                        int bedwarsFoursOldKills = oldBedwarsObject.optInt("four_four_kills_bedwars");
                        int bedwarsFoursOldDeaths = oldBedwarsObject.optInt("four_four_deaths_bedwars");
                        double bedwarsFoursOldKD = Utils.round(bedwarsFoursOldKills / (double) bedwarsFoursOldDeaths, 3);
                        int bedwarsFoursOldFinalKills = oldBedwarsObject.optInt("four_four_final_kills_bedwars");
                        int bedwarsFoursOldFinalDeaths = oldBedwarsObject.optInt("four_four_final_deaths_bedwars");
                        double bedwarsFoursOldFKD = Utils.round(bedwarsFoursOldFinalKills / (double) bedwarsFoursOldFinalDeaths, 3);

                        int bedwarsFoursWinstreak = bedwarsObject.optInt("four_four_winstreak");
                        int bedwarsFoursWins = bedwarsObject.optInt("four_four_wins_bedwars");
                        int bedwarsFoursLosses = bedwarsObject.optInt("four_four_losses_bedwars");
                        double bedwarsFoursWL = Utils.round(bedwarsFoursWins / (double) bedwarsFoursLosses, 3);
                        int bedwarsFoursKills = bedwarsObject.optInt("four_four_kills_bedwars");
                        int bedwarsFoursDeaths = bedwarsObject.optInt("four_four_deaths_bedwars");
                        double bedwarsFoursKD = Utils.round(bedwarsFoursKills / (double) bedwarsFoursDeaths, 3);
                        int bedwarsFoursFinalKills = bedwarsObject.optInt("four_four_final_kills_bedwars");
                        int bedwarsFoursFinalDeaths = bedwarsObject.optInt("four_four_final_deaths_bedwars");
                        double bedwarsFoursFKD = Utils.round(bedwarsFoursFinalKills / (double) bedwarsFoursFinalDeaths, 3);

                        int bedwarsFoursWinstreakDifference = bedwarsFoursWinstreak - bedwarsFoursOldWinstreak;
                        int bedwarsFoursWinsDifference = bedwarsFoursWins - bedwarsFoursOldWins;
                        int bedwarsFoursLossesDifference = bedwarsFoursLosses - bedwarsFoursOldLosses;
                        double bedwarsFoursWLDifference = Utils.round(bedwarsFoursWL - bedwarsFoursOldWL, 3);
                        int bedwarsFoursKillsDifference = bedwarsFoursKills - bedwarsFoursOldKills;
                        int bedwarsFoursDeathsDifference = bedwarsFoursDeaths - bedwarsFoursOldDeaths;
                        double bedwarsFoursKDDifference = Utils.round(bedwarsFoursKD - bedwarsFoursOldKD, 3);
                        int bedwarsFoursFinalKillsDifference = bedwarsFoursFinalKills - bedwarsFoursOldFinalKills;
                        int bedwarsFoursFinalDeathsDifference = bedwarsFoursFinalDeaths - bedwarsFoursOldFinalDeaths;
                        double bedwarsFoursFKDDifference = Utils.round(bedwarsFoursFKD - bedwarsFoursOldFKD, 3);

                        showChangesField = bedwarsFoursWinsDifference != 0 || bedwarsFoursLossesDifference != 0 || bedwarsFoursWLDifference != 0.0 || bedwarsFoursKillsDifference != 0 || bedwarsFoursDeathsDifference != 0 || bedwarsFoursKDDifference != 0;

                        changesFieldValue.append("\u2022Wins: ").append(bedwarsFoursWins).append(" (").append(bedwarsFoursWinsDifference > 0 ? "+" + bedwarsFoursWinsDifference : bedwarsFoursWinsDifference).append(")\n");
                        changesFieldValue.append("\u2022Losses: ").append(bedwarsFoursLosses).append(" (").append(bedwarsFoursLossesDifference > 0 ? "+" + bedwarsFoursLossesDifference : bedwarsFoursLossesDifference).append(")\n");
                        changesFieldValue.append("\u2022W/L: ").append(bedwarsFoursWL).append(" (").append(bedwarsFoursWLDifference > 0 ? "+" + bedwarsFoursWLDifference : bedwarsFoursWLDifference).append(")\n");
                        changesFieldValue.append("\u2022Kills: ").append(bedwarsFoursKills).append(" (").append(bedwarsFoursKillsDifference > 0 ? "+" + bedwarsFoursKillsDifference : bedwarsFoursKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Deaths: ").append(bedwarsFoursDeaths).append(" (").append(bedwarsFoursDeathsDifference > 0 ? "+" + bedwarsFoursDeathsDifference : bedwarsFoursDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022K/D: ").append(bedwarsFoursKD).append(" (").append(bedwarsFoursKDDifference > 0 ? "+" + bedwarsFoursKDDifference : bedwarsFoursKDDifference).append(")\n");
                        changesFieldValue.append("\u2022Final Kills: ").append(bedwarsFoursFinalKills).append(" (").append(bedwarsFoursFinalKillsDifference > 0 ? "+" + bedwarsFoursFinalKillsDifference : bedwarsFoursFinalKillsDifference).append(")\n");
                        changesFieldValue.append("\u2022Final Deaths: ").append(bedwarsFoursFinalDeaths).append(" (").append(bedwarsFoursFinalDeathsDifference > 0 ? "+" + bedwarsFoursFinalDeathsDifference : bedwarsFoursFinalDeathsDifference).append(")\n");
                        changesFieldValue.append("\u2022FK/D: ").append(bedwarsFoursFKD).append(" (").append(bedwarsFoursFKDDifference > 0 ? "+" + bedwarsFoursFKDDifference : bedwarsFoursFKDDifference).append(")\n");
                        changesFieldValue.append("\u2022Winstreak: ").append(bedwarsFoursWinstreak).append(" (").append(bedwarsFoursWinstreakDifference > 0 ? "+" + bedwarsFoursWinstreakDifference : bedwarsFoursWinstreakDifference).append(")\n");

                        changesFieldValue.append("`");

                        if (showChangesField)
                            bedwarsStatsEmbed.addField("Bedwars Fours", changesFieldValue.toString(), false);

                        //endregion

                        if (bedwarsStatsEmbed.getFields().size() > 0) embeds.add(bedwarsStatsEmbed);

                        embeds.forEach(embed -> Objects.requireNonNull(BotMain.bot.getTextChannelById(channelToSendTo)).sendMessage(embed.build()).queue());
                        BotMain.oldTrackerPlayerObjectHashMap.replace(key, newPlayerObject);
                    }


                } else {
                    BotMain.oldTrackerPlayerObjectHashMap.put(key, newPlayerObject);
                    if (newPlayerObject.getStatus().equals("Online")) {
                        connectEmbed.setTitle(newPlayerObject.getUsername() + " connected");
                        connectEmbed.addField("Started tracking stats", Utils.parseDate(new Date()), true);
                        connectEmbed.setColor(RankUtils.Ranks.VIP.getColor());
                        Objects.requireNonNull(BotMain.bot.getTextChannelById(channelToSendTo)).sendMessage(connectEmbed.build()).queue();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
