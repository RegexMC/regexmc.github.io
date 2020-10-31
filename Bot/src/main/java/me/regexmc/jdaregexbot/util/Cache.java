package me.regexmc.jdaregexbot.util;

import me.regexmc.jdaregexbot.BotMain;
import me.regexmc.jdaregexbot.gameobjects.bedwars.BedwarsSetter;
import me.regexmc.jdaregexbot.gameobjects.skywars.SkywarsSetter;
import net.hypixel.api.util.ILeveling;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Cache {
    public static Player getPlayerFromFile(String path) throws IOException {
        JSONObject playerCacheJSON = new JSONObject(Utils.readFile(path));

        return createPlayer(playerCacheJSON.toString(), true);
    }

    public static Player createPlayer(String player, Boolean cached) throws IOException {
        Player p = new Player();
        JSONObject playerObject = new JSONObject(player);

        p.setUuid((String) playerObject.get("uuid"));
        p.setRank(APIUtil.getRank(playerObject, cached));
        p.setLastLogin(playerObject.get("lastLogin").toString());
        p.setNetworkExp(Double.parseDouble(playerObject.get("networkExp").toString()));
        p.setNetworkLevel(Utils.round(ILeveling.getExactLevel(Double.parseDouble(playerObject.get("networkExp").toString())), 2));
        p.setKarma(playerObject.get("karma").toString());
        p.setAchievementPoints(APIUtil.achievementPoints(playerObject));
        p.setAchievements(playerObject.getJSONObject("achievements"));
        p.setAchievementsOneTime(playerObject.getJSONArray("achievementsOneTime"));

        JSONObject swStats = playerObject.getJSONObject("stats").getJSONObject(cached ? "skywars" : "SkyWars");
        JSONObject bwStats = playerObject.getJSONObject("stats").getJSONObject(cached ? "bedwars" : "Bedwars");

        p.setSkywarsStats(SkywarsSetter.setStats(swStats, cached));
        p.setBedwarsStats(BedwarsSetter.setStats(bwStats, cached));

        p.setPlayerObject(p);
        p.pushStats();

        return p;
    }

    public static List<String> clearCache() {
        List<String> results = new ArrayList<>();
        File[] files = new File(BotMain.config.get("path_json").toString()).listFiles();

        for (File file : files) {
            if (file.isFile()) {
                if (file.getName().startsWith("obj_") && file.getAbsolutePath().endsWith(".json")) {
                    try {
                        if (file.delete()) {
                            Utils.log("Deleted " + file.getName(), Utils.ErrorTypes.INFO);
                            results.add("Deleted " + file.getName());
                        }
                    } catch (SecurityException e) {
                        Utils.log(e, Utils.ErrorTypes.ERROR);
                        e.printStackTrace();
                    }
                }
            }
        }
        return results;
    }

    public static String getUUIDFromCache(String player) throws IOException {
        JSONObject playerUUIDS = new JSONObject(Utils.readFile(BotMain.config.get("path_json") + "playerUUIDS.json"));
        if (playerUUIDS.has(player.toLowerCase())) {
            return playerUUIDS.get(player.toLowerCase()).toString();
        } else {
            try {
                String UUID = APIUtil.getUUIDFromName(player);
                if (UUID.equals("Invalid Username")) return UUID;
                if (playerUUIDS.has(UUID)) {
                    //name changed (name isnt there but UUID is)
                    Iterator<String> keys = playerUUIDS.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (playerUUIDS.get(key).equals(UUID)) {
                            playerUUIDS.remove(key);
                            Utils.writeToFile(BotMain.config.get("path_json") + "playerUUIDS.json", playerUUIDS.toString());
                            break;
                        }
                    }
                }
                Cache.addUUIDToCache(player, UUID);
                return UUID;
            } catch (Exception e) {
                Utils.log(e, Utils.ErrorTypes.ERROR);
                e.printStackTrace();
                return "Invalid Username";
            }
        }
    }

    public static void addUUIDToCache(String name, String uuid) throws IOException {
        JSONObject playerUUIDS = new JSONObject(Utils.readFile(BotMain.config.get("path_json") + "playerUUIDS.json"));
        playerUUIDS.put(name.toLowerCase(), uuid);
        Utils.writeToFile(BotMain.config.get("path_json") + "playerUUIDS.json", playerUUIDS.toString());
    }

    public static Player handle(String UUID) throws IOException, ExecutionException, InterruptedException {
        String playerCacheDir = BotMain.config.get("path_json") + "obj_" + UUID;
        Player p;

        if (Utils.exists(playerCacheDir)) {
            if (Utils.timeSinceLastEdit(playerCacheDir) > Utils.minutesToMs(5)) {
                String player = APIUtil.API.getPlayerByUuid(UUID).get().getPlayer().toString();
                Utils.log("Called Hypixel API", Utils.ErrorTypes.API);
                if (player == null) return null;
                p = Cache.createPlayer(player, false);
                p.setPlayerObject(p);
                Utils.writeToFile(playerCacheDir, p.getPlayerObject().toString());
            } else {
                p = Cache.getPlayerFromFile(playerCacheDir);
            }
        } else {
            try {
                String player = APIUtil.API.getPlayerByUuid(UUID).get().getPlayer().toString();
                Utils.log("Called Hypixel API", Utils.ErrorTypes.API);
                if (player == null) return null;
                p = Cache.createPlayer(player, false);
                p.setPlayerObject(p);
            } catch (NullPointerException e) {
                return null;
            }
            File file = new File(playerCacheDir);
            if (file.createNewFile()) {
                Utils.log("File created: " + file.getName(), Utils.ErrorTypes.INFO);
            } else {
                Utils.log("File already exists.", Utils.ErrorTypes.WARNING);
            }

            Utils.writeToFile(playerCacheDir, p.getPlayerObject().toString());

        }

        return p;
    }
}
