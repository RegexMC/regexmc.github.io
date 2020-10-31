package me.regexmc.jdaregexbot.gameobjects.bedwars;

import org.json.JSONObject;

public class BedwarsObject {

    private final JSONObject bedwarsObject = new JSONObject();
    private int kills;
    private int finalKills;
    private int deaths;
    private int finalDeaths;
    private int wins;
    private int losses;
    private int experience;
    private double level;

    public JSONObject getBedwarsObject() {
        return bedwarsObject;
    }


    public void setBedwarsObject(BedwarsObject bwObject) {
        bedwarsObject.put("kills", bwObject.getKills());
        bedwarsObject.put("finalKills", bwObject.getFinalKills());
        bedwarsObject.put("deaths", bwObject.getDeaths());
        bedwarsObject.put("finalDeaths", bwObject.getFinalDeaths());
        bedwarsObject.put("wins", bwObject.getWins());
        bedwarsObject.put("losses", bwObject.getLosses());
        bedwarsObject.put("experience", bwObject.getExperience());
        bedwarsObject.put("level", bwObject.getLevel());
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getFinalKills() {
        return finalKills;
    }

    public void setFinalKills(int finalKills) {
        this.finalKills = finalKills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getFinalDeaths() {
        return finalDeaths;
    }

    public void setFinalDeaths(int finalDeaths) {
        this.finalDeaths = finalDeaths;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    public void setSoloBedwars(JSONObject bedwarsSoloObject) {
        this.bedwarsObject.put("solo", bedwarsSoloObject);
    }

    public void setDuosBedwars(JSONObject bedwarsDuosObject) {
        this.bedwarsObject.put("duos", bedwarsDuosObject);
    }

    public void setThreesBedwars(JSONObject bedwarsThreesObject) {
        this.bedwarsObject.put("threes", bedwarsThreesObject);
    }

}
