package me.regexmc.jdaregexbot.gameobjects.skywars;

import me.regexmc.jdaregexbot.util.APIUtil;
import me.regexmc.jdaregexbot.util.Utils;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Map;
import java.util.TreeMap;

public class SkywarsSetter {
    private static SkywarsObject skywarsObject;

    public static JSONObject setStats(JSONObject swStats, Boolean cached) {

        int kills = swStats.has("kills") ? swStats.getInt("kills") : 0;
        int deaths = swStats.has("deaths") ? swStats.getInt("deaths") : 0;

        int wins = swStats.has("wins") ? swStats.getInt("wins") : 0;
        int losses = swStats.has("losses") ? swStats.getInt("losses") : 0;
        double experience = swStats.has(cached ? "experience" : "skywars_experience") ? swStats.getInt(cached ? "experience" : "skywars_experience") : 0.0;
        double level = APIUtil.getSkywarsLevel((int) experience);
        int heads = swStats.has("heads") ? swStats.getInt("heads") : 0;
        //int playtime;
        //int corruptionChance ;
        int shards = swStats.has(cached ? "shards" : "shard") ? swStats.getInt(cached ? "shards" : "shard") : 0;
        int opals = swStats.has("opals") ? swStats.getInt("opals") : 0;
        long playtime = swStats.has("time_played") ? Long.parseLong(swStats.get("time_played").toString()) : 0;

        int soloNormalKills = getSoloNormal(swStats, "kills", cached);
        int soloNormalDeaths = getSoloNormal(swStats, "deaths", cached);
        int soloNormalWins = getSoloNormal(swStats, "wins", cached);
        int soloNormalLosses = getSoloNormal(swStats, "losses", cached);

        int soloInsaneKills = getSoloInsane(swStats, "kills", cached);
        int soloInsaneDeaths = getSoloInsane(swStats, "deaths", cached);
        int soloInsaneWins = getSoloInsane(swStats, "wins", cached);
        int soloInsaneLosses = getSoloInsane(swStats, "losses", cached);

        int teamsNormalKills = getTeamsNormal(swStats, "kills", cached);
        int teamsNormalDeaths = getTeamsNormal(swStats, "deaths", cached);
        int teamsNormalWins = getTeamsNormal(swStats, "wins", cached);
        int teamsNormalLosses = getTeamsNormal(swStats, "losses", cached);

        int teamsInsaneKills = getTeamsInsane(swStats, "kills", cached);
        int teamsInsaneDeaths = getTeamsInsane(swStats, "deaths", cached);
        int teamsInsaneWins = getTeamsInsane(swStats, "wins", cached);
        int teamsInsaneLosses = getTeamsInsane(swStats, "losses", cached);

        int rankedKills = getRanked(swStats, "kills", cached);
        int rankedDeaths = getRanked(swStats, "deaths", cached);
        int rankedWins = getRanked(swStats, "wins", cached);
        int rankedLosses = getRanked(swStats, "losses", cached);

        skywarsObject = new SkywarsObject();
        skywarsObject.setKills(kills);
        skywarsObject.setDeaths(deaths);
        skywarsObject.setWins(wins);
        skywarsObject.setLosses(losses);
        skywarsObject.setExperience((int) experience);
        skywarsObject.setLevel(level);
        skywarsObject.setHeads(heads);
        skywarsObject.setShards(shards);
        skywarsObject.setOpals(opals);
        skywarsObject.setPlaytime(playtime);

        JSONObject soloNormalObject = new JSONObject();
        soloNormalObject.put("kills", soloNormalKills);
        soloNormalObject.put("deaths", soloNormalDeaths);
        soloNormalObject.put("wins", soloNormalWins);
        soloNormalObject.put("losses", soloNormalLosses);
        skywarsObject.setSoloNormalObject(soloNormalObject);

        JSONObject soloInsaneObject = new JSONObject();
        soloInsaneObject.put("kills", soloInsaneKills);
        soloInsaneObject.put("deaths", soloInsaneDeaths);
        soloInsaneObject.put("wins", soloInsaneWins);
        soloInsaneObject.put("losses", soloInsaneLosses);
        skywarsObject.setSoloInsaneObject(soloInsaneObject);

        JSONObject teamsNormalObject = new JSONObject();
        teamsNormalObject.put("kills", teamsNormalKills);
        teamsNormalObject.put("deaths", teamsNormalDeaths);
        teamsNormalObject.put("wins", teamsNormalWins);
        teamsNormalObject.put("losses", teamsNormalLosses);
        skywarsObject.setTeamsNormal(teamsNormalObject);

        JSONObject teamsInsaneObject = new JSONObject();
        teamsInsaneObject.put("kills", teamsInsaneKills);
        teamsInsaneObject.put("deaths", teamsInsaneDeaths);
        teamsInsaneObject.put("wins", teamsInsaneWins);
        teamsInsaneObject.put("losses", teamsInsaneLosses);
        skywarsObject.setTeamsInsane(teamsInsaneObject);

        RankedObject rankedObject = new RankedObject();
        rankedObject.setKills(rankedKills);
        rankedObject.setDeaths(rankedDeaths);
        rankedObject.setWins(rankedWins);
        rankedObject.setLosses(rankedLosses);

        Period diff = Period.between(LocalDate.of(2016, Month.JANUARY, 1), LocalDate.now());
        int years = diff.getYears() + 1;

        if (cached) {
            TreeMap<Integer, int[]> robj = Utils.getSeasons((JSONObject) swStats.get("ranked"));
            for (Map.Entry<Integer, int[]> entry : robj.entrySet()) {
                RankedObject.SeasonObj seasonObj = new RankedObject.SeasonObj();
                seasonObj.setRating(entry.getValue()[0]);
                seasonObj.setPos(entry.getValue()[1]);
                seasonObj.setSeasonObject(seasonObj);

                rankedObject.pushSeasonsObject(entry.getKey(), seasonObj.getSeasonObject());
            }
        } else {
            int start = -2;
            for (int i = 16; i < years + 16; i++) {
                for (int j = 1; j < 13; j++) {
                    int season = start;
                    start++;

                    String formattedRatingKey = "SkyWars_skywars_rating_" + j + "_" + i + "_rating";
                    if (swStats.has(formattedRatingKey)) {
                        RankedObject.SeasonObj seasonObj = new RankedObject.SeasonObj();
                        seasonObj.setRating(swStats.getInt(formattedRatingKey));
                        seasonObj.setPos(swStats.getInt("SkyWars_skywars_rating_" + j + "_" + i + "_position"));
                        seasonObj.setSeasonObject(seasonObj);
                        rankedObject.pushSeasonsObject(season, seasonObj.getSeasonObject());
                    }
                }
            }
        }

        rankedObject.setRankedObject(rankedObject);
        skywarsObject.setRanked(rankedObject.getRankedObject());
        skywarsObject.setSkywarsObject(skywarsObject);

        return skywarsObject.getSkywarsObject();
    }

    public static int getSoloNormal(JSONObject swStats, String key, Boolean cached) {
        if (cached) return Utils.parseInt(swStats.getJSONObject("solo_normal").get(key));
        return swStats.has(key + "_" + "solo_normal") ? Utils.parseInt(swStats.get(key + "_" + "solo_normal")) : 0;
    }

    public static int getSoloInsane(JSONObject swStats, String key, Boolean cached) {
        if (cached) return Utils.parseInt(swStats.getJSONObject("solo_insane").get(key));
        return swStats.has(key + "_" + "solo_insane") ? Utils.parseInt(swStats.get(key + "_" + "solo_insane")) : 0;
    }

    public static int getTeamsNormal(JSONObject swStats, String key, Boolean cached) {
        if (cached) return Utils.parseInt(swStats.getJSONObject("teams_normal").get(key));
        return swStats.has(key + "_" + "team_normal") ? Utils.parseInt(swStats.get(key + "_" + "team_normal")) : 0;
    }

    public static int getTeamsInsane(JSONObject swStats, String key, Boolean cached) {
        if (cached) return Utils.parseInt(swStats.getJSONObject("teams_insane").get(key));
        return swStats.has(key + "_" + "team_insane") ? Utils.parseInt(swStats.get(key + "_" + "team_insane")) : 0;
    }

    public static int getRanked(JSONObject swStats, String key, Boolean cached) {
        if (cached) return Utils.parseInt(swStats.getJSONObject("ranked").get(key));
        return swStats.has(key + "_" + "ranked_normal") ? Utils.parseInt(swStats.get(key + "_" + "ranked_normal")) : 0;
    }

    public static SkywarsObject getSkywarsObject() {
        return skywarsObject;
    }
}
