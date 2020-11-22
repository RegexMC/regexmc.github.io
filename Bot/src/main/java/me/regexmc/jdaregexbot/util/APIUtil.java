package me.regexmc.jdaregexbot.util;

import me.regexmc.jdaregexbot.BotMain;
import net.hypixel.api.HypixelAPI;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

public class APIUtil {
    public static HypixelAPI API;
    private static int total = 0;

    public static String getUUIDFromName(String name) throws IOException {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
        JSONObject mojangAPIJson = Utils.readJsonFromUrl(url);
        Utils.log("Called Mojang API", Utils.ErrorTypes.API);
        if (mojangAPIJson == null) return "Invalid Username";
        return mojangAPIJson.get("id").toString();
    }

    public static RankUtils.Ranks getRank(JSONObject player, Boolean cached) {
        String rank;
        if (player.has("rank")) {
            rank = player.get("rank").toString();
            if (cached) {
                return RankUtils.Ranks.getRankFromString(rank);
            }
            if (!rank.equalsIgnoreCase("normal")) {
                if (rank.equalsIgnoreCase("HELPER")) {
                    return RankUtils.Ranks.HELPER;
                } else if (rank.equalsIgnoreCase("MODERATOR")) {
                    return RankUtils.Ranks.MODERATOR;
                } else if (rank.equalsIgnoreCase("ADMIN")) {
                    return RankUtils.Ranks.ADMIN;
                } else if (rank.equalsIgnoreCase("YOUTUBER")) {
                    return RankUtils.Ranks.YOUTUBE;
                }
            }
        }
        if (player.has("monthlyPackageRank")) {
            rank = player.get("monthlyPackageRank").toString();
            if (rank.equalsIgnoreCase("superstar")) {
                return RankUtils.Ranks.MVP_PLUSPLUS;
            }
        }
        if (player.has("newPackageRank")) {
            rank = player.get("newPackageRank").toString();
            if (rank != null && !rank.equalsIgnoreCase("null")) {
                if (rank.equalsIgnoreCase("MVP_PLUS")) {
                    return RankUtils.Ranks.MVP_PLUS;
                } else if (rank.equalsIgnoreCase("MVP")) {
                    return RankUtils.Ranks.MVP;
                } else if (rank.equalsIgnoreCase("VIP_PLUS")) {
                    return RankUtils.Ranks.VIP_PLUS;
                } else if (rank.equalsIgnoreCase("VIP")) {
                    return RankUtils.Ranks.VIP;
                } else {
                    return RankUtils.Ranks.NONE;
                }
            }
        }

        return RankUtils.Ranks.NONE;
    }

    public static int achievementPoints(JSONObject player) throws IOException {
        total = 0;

        JSONObject serverAchievements = (JSONObject) new JSONObject(new String(Files.readAllBytes(Path.of(BotMain.config.get("path_json") + "server_aps.json")))).get("achievements");
        JSONArray playerOneTimeAps = (JSONArray) player.get("achievementsOneTime");
        JSONObject playerTieredAps = (JSONObject) player.get("achievements");

        playerOneTimeAps.forEach(ap -> {
            String game = ap.toString().split("_", 2)[0].toLowerCase();
            if (game.equals("bridge")) {
                game = "duels";
            }
            String achievementName = ap.toString().split("_", 2)[1].toUpperCase();
            JSONObject HypixelProvidedJSONGame = (JSONObject) serverAchievements.get(game);
            JSONObject GameOneTime = (JSONObject) HypixelProvidedJSONGame.get("one_time");
            Iterator<String> keys = GameOneTime.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.equals(achievementName)) {
                    total += Integer.parseInt(((JSONObject) GameOneTime.get(achievementName)).get("points").toString());
                }
            }
        });

        serverAchievements.keys();
        Iterator<String> keys;
        keys = serverAchievements.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (serverAchievements.get(key) instanceof JSONObject) {
                JSONObject game_tieredObject = (JSONObject) ((JSONObject) serverAchievements.get(key)).get("tiered");
                Set<String> tieredApKeys = playerTieredAps.keySet();
                tieredApKeys.forEach(ap -> {
                    if (ap.startsWith(key)) {
                        String trim = ap.substring(ap.indexOf("_") + 1).trim();
                        if (game_tieredObject.has(trim.toUpperCase())) {
                            int amt = playerTieredAps.getInt(ap);
                            JSONObject game_ap = (JSONObject) game_tieredObject.get(trim.toUpperCase());
                            if (game_ap.has("tiers")) {
                                JSONArray game_ap_tiers = (JSONArray) game_ap.get("tiers");
                                for (int i = 0; i < game_ap_tiers.length(); i++) {
                                    if (game_ap_tiers.length() > i) {
                                        int needed = Integer.parseInt(((JSONObject) game_ap_tiers.get(i)).get("amount").toString());
                                        if (amt >= needed) {
                                            int tierOneAps = Integer.parseInt(((JSONObject) game_ap_tiers.get(i)).get("points").toString());
                                            total += tierOneAps;
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }

        return total;
    }

    public static double getSkywarsLevel(int experience) {
        int[] xps = new int[]{0, 20, 70, 150, 250, 500, 1000, 2000, 3500, 6000, 10000, 15000};
        double exactLevel = 0.0;
        if (experience >= 15000) {
            exactLevel = ((double) experience - 15000) / 10000 + 12;
        } else {
            for (int i = 0; i < xps.length; i++) {
                if (experience < xps[i]) {
                    exactLevel = i + ((double) experience - xps[i - 1]) / (xps[i] - xps[i - 1]);
                    break;
                }
            }
        }
        return exactLevel;
    }
}