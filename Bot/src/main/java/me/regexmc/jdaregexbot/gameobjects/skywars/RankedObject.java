package me.regexmc.jdaregexbot.gameobjects.skywars;

import org.json.JSONObject;

public class RankedObject {
    private final JSONObject rankedObject = new JSONObject();
    private int kills;
    private int deaths;
    private int wins;
    private int losses;

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

    public void pushSeasonsObject(int season, JSONObject seasonObj) {
        rankedObject.put("season_" + season, seasonObj);
    }

    public JSONObject getRankedObject() {
        return rankedObject;
    }

    public void setRankedObject(RankedObject rankedObject) {
        this.rankedObject.put("kills", rankedObject.getKills());
        this.rankedObject.put("deaths", rankedObject.getDeaths());
        this.rankedObject.put("wins", rankedObject.getWins());
        this.rankedObject.put("losses", rankedObject.getLosses());
    }

    public static class SeasonObj {
        private final JSONObject seasonObject = new JSONObject();
        private int rating;
        private int pos;

        public JSONObject getSeasonObject() {
            return seasonObject;
        }

        public void setSeasonObject(SeasonObj seasonObj) {
            seasonObject.put("rating", seasonObj.getRating());
            seasonObject.put("pos", seasonObj.getPos());
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public int getPos() {
            return pos;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }
    }
}
