package me.regexmc.jdaregexbot.gameobjects.bedwars;

import org.json.JSONObject;

public class BedwarsSetter {
    private static BedwarsObject bedwarsObject;

    public static JSONObject setStats(JSONObject bwStats, Boolean cached) {

        int kills = (int) getOverall(bwStats, "kills", cached);
        int deaths = (int) getOverall(bwStats, "deaths", cached);
        int wins = (int) getOverall(bwStats, "wins", cached);
        int losses = (int) getOverall(bwStats, "losses", cached);

        int soloKills = (int) getSolo(bwStats, "kills", cached);
        int soloDeaths = (int) getSolo(bwStats, "deaths", cached);
        int soloWins = (int) getSolo(bwStats, "wins", cached);
        int soloLosses = (int) getSolo(bwStats, "losses", cached);

        int duosKills = (int) getDuos(bwStats, "kills", cached);
        int duosDeaths = (int) getDuos(bwStats, "deaths", cached);
        int duosWins = (int) getDuos(bwStats, "wins", cached);
        int duosLosses = (int) getDuos(bwStats, "losses", cached);

        int threesKills = (int) getThrees(bwStats, "kills", cached);
        int threesDeaths = (int) getThrees(bwStats, "deaths", cached);
        int threesWins = (int) getThrees(bwStats, "wins", cached);
        int threesLosses = (int) getThrees(bwStats, "losses", cached);


        bedwarsObject = new BedwarsObject();
        bedwarsObject.setKills(kills);
        bedwarsObject.setDeaths(deaths);
        bedwarsObject.setWins(wins);
        bedwarsObject.setLosses(losses);

        JSONObject soloBedwarsObject = new JSONObject();
        soloBedwarsObject.put("kills", soloKills);
        soloBedwarsObject.put("deaths", soloDeaths);
        soloBedwarsObject.put("wins", soloWins);
        soloBedwarsObject.put("losses", soloLosses);

        JSONObject duosBedwarsObject = new JSONObject();
        duosBedwarsObject.put("kills", duosKills);
        duosBedwarsObject.put("deaths", duosDeaths);
        duosBedwarsObject.put("wins", duosWins);
        duosBedwarsObject.put("losses", duosLosses);

        JSONObject threesBedwarsObject = new JSONObject();
        threesBedwarsObject.put("kills", threesKills);
        threesBedwarsObject.put("deaths", threesDeaths);
        threesBedwarsObject.put("wins", threesWins);
        threesBedwarsObject.put("losses", threesLosses);

        bedwarsObject.setSoloBedwars(soloBedwarsObject);
        bedwarsObject.setDuosBedwars(duosBedwarsObject);
        bedwarsObject.setThreesBedwars(threesBedwarsObject);
        bedwarsObject.setBedwarsObject(bedwarsObject);
        return bedwarsObject.getBedwarsObject();
    }

    public static BedwarsObject getBedwarsObject() {
        return bedwarsObject;
    }

    private static Object getOverall(JSONObject bwStats, String key, Boolean cached) {
        return cached ? bwStats.get(key) : (bwStats.has(key + "_bedwars") ? bwStats.get(key + "_bedwars") : 0);
    }

    private static Object getSolo(JSONObject bwStats, String key, Boolean cached) {
        return cached ? bwStats.getJSONObject("solo").get(key) : (bwStats.has("eight_one_" + key + "_bedwars") ? bwStats.get("eight_one_" + key + "_bedwars") : 0);
    }

    private static Object getDuos(JSONObject bwStats, String key, Boolean cached) {
        return cached ? bwStats.getJSONObject("duos").get(key) : (bwStats.has("eight_two_" + key + "_bedwars") ? bwStats.get("eight_two_" + key + "_bedwars") : 0);
    }

    private static Object getThrees(JSONObject bwStats, String key, Boolean cached) {
        return cached ? bwStats.getJSONObject("threes").get(key) : (bwStats.has("four_three_" + key + "_bedwars") ? bwStats.get("four_three_" + key + "_bedwars") : 0);
    }
}
