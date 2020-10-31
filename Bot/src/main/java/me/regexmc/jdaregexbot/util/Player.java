package me.regexmc.jdaregexbot.util;

import org.json.JSONArray;
import org.json.JSONObject;

public class Player {

    private final JSONObject playerObject = new JSONObject();
    private final JSONObject stats = new JSONObject();
    private String uuid;
    private RankUtils.Ranks rank;
    private String lastLogin;
    private double networkExp;
    private double networkLevel;
    private String karma;
    private int achievementPoints;
    private JSONObject achievements;
    private JSONArray achievementsOneTime;

    public JSONObject getStats() {
        return stats;
    }

    public void setSkywarsStats(JSONObject statsObj) {
        stats.put("skywars", statsObj);
    }

    public void setBedwarsStats(JSONObject statsObj) {
        stats.put("bedwars", statsObj);
    }

    public void pushStats() {
        playerObject.put("stats", stats);
    }

    public JSONObject getPlayerObject() {
        return playerObject;
    }

    public void setPlayerObject(Player player) {
        playerObject.put("uuid", player.getUuid());
        playerObject.put("rank", player.getRank());
        playerObject.put("lastLogin", player.getLastLogin());
        playerObject.put("networkExp", player.getNetworkExp());
        playerObject.put("karma", player.getKarma());
        playerObject.put("achievements", player.getAchievements());
        playerObject.put("achievementsOneTime", player.getAchievementsOneTime());
    }

    public int getAchievementPoints() {
        return achievementPoints;
    }

    public void setAchievementPoints(int achievementPoints) {
        this.achievementPoints = achievementPoints;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double getNetworkLevel() {
        return networkLevel;
    }

    public void setNetworkLevel(double networkLevel) {
        this.networkLevel = networkLevel;
    }

    public JSONObject getAchievements() {
        return achievements;
    }

    public void setAchievements(JSONObject achievements) {
        this.achievements = achievements;
    }

    public JSONArray getAchievementsOneTime() {
        return achievementsOneTime;
    }

    public void setAchievementsOneTime(JSONArray achievementsOneTime) {
        this.achievementsOneTime = achievementsOneTime;
    }

    public String getKarma() {
        return karma;
    }

    public void setKarma(String karma) {
        this.karma = karma;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public double getNetworkExp() {
        return networkExp;
    }

    public void setNetworkExp(double networkExp) {
        this.networkExp = networkExp;
    }

    public RankUtils.Ranks getRank() {
        return rank;
    }

    public void setRank(RankUtils.Ranks rank) {
        this.rank = rank;
    }
}
