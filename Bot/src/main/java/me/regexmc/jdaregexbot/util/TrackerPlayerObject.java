package me.regexmc.jdaregexbot.util;

import org.json.JSONObject;

import java.util.Date;

public class TrackerPlayerObject {
    private final String status;

    private final String username;
    private final String UUID;
    private final JSONObject stats;
    private final Date setDate;

    public TrackerPlayerObject(JSONObject playerObject, Date now) {
        this.status = playerObject.optLong("lastLogin") > playerObject.optLong("lastLogout") ? "Online" : "Offline";
        this.username = playerObject.optString("displayname");
        this.UUID = playerObject.optString("uuid");
        this.stats = playerObject.getJSONObject("stats");
        this.setDate = now;
    }

    public String getStatus() {
        return status;
    }

    public JSONObject getStats() {
        return stats;
    }

    public Date getSetDate() {
        return setDate;
    }

    public String getUsername() {
        return username;
    }

    public String getUUID() {
        return UUID;
    }
}
