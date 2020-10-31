package me.regexmc.jdaregexbot.gameobjects.skywars;

import org.json.JSONObject;

public class SkywarsObject {
    private final JSONObject skywarsObject = new JSONObject();
    private int kills;
    private int deaths;
    private int wins;
    private int losses;
    private double experience;
    private double level;
    private int heads;
    private long playtime;
    private int corruptionChance;
    private int shards;
    private int opals;

    public JSONObject getSkywarsObject() {
        return skywarsObject;
    }

    public void setSkywarsObject(SkywarsObject swObject) {
        skywarsObject.put("kills", swObject.getKills());
        skywarsObject.put("deaths", swObject.getDeaths());
        skywarsObject.put("wins", swObject.getWins());
        skywarsObject.put("losses", swObject.getLosses());
        skywarsObject.put("experience", swObject.getExperience());
        skywarsObject.put("level", swObject.getLevel());
        skywarsObject.put("heads", swObject.getHeads());
        skywarsObject.put("shards", swObject.getShards());
        skywarsObject.put("opals", swObject.getOpals());
        skywarsObject.put("time_played", swObject.getPlaytime());
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
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

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = experience;
    }

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    public int getHeads() {
        return heads;
    }

    public void setHeads(int heads) {
        this.heads = heads;
    }

    public long getPlaytime() {
        return playtime;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    public int getCorruptionChance() {
        return corruptionChance;
    }

    public void setCorruptionChance(int corruptionChance) {
        this.corruptionChance = corruptionChance;
    }

    public int getShards() {
        return shards;
    }

    public void setShards(int shards) {
        this.shards = shards;
    }

    public int getOpals() {
        return opals;
    }

    public void setOpals(int opals) {
        this.opals = opals;
    }

    public void setSoloNormalObject(JSONObject soloNormalObject) {
        this.skywarsObject.put("solo_normal", soloNormalObject);
    }

    public void setSoloInsaneObject(JSONObject soloInsaneObject) {
        this.skywarsObject.put("solo_insane", soloInsaneObject);
    }

    public void setTeamsNormal(JSONObject teamsNormalObject) {
        this.skywarsObject.put("teams_normal", teamsNormalObject);
    }

    public void setTeamsInsane(JSONObject teamsInsaneObject) {
        this.skywarsObject.put("teams_insane", teamsInsaneObject);
    }

    public void setRanked(JSONObject rankedObject) {
        this.skywarsObject.put("ranked", rankedObject);
    }
}
