package me.regexmc.jdaregexbot.commands.admin.anime;

import com.google.gson.JsonParser;
import me.regexmc.jdaregexbot.BotMain;
import me.regexmc.jdaregexbot.util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class AnimeNotification extends TimerTask {
    public void run() {
        StringBuilder msg = new StringBuilder();

        try {
            List<Object> animeIds = new JSONObject(JsonParser.parseString(Utils.loadResourceAsString("anime.json")).getAsJsonObject().toString())
                    .getJSONArray("ids").toList();

            StringBuilder query = new StringBuilder();
            query.append("{\"query\":\"{");

            AtomicInteger c = new AtomicInteger(1);

            animeIds.forEach(o -> {
                c.getAndIncrement();
                query.append(getCharForNumber(c.get() - 1)).append(":Media(id:").append(o).append("){title{romaji english}airingSchedule{nodes{episode airingAt timeUntilAiring}}}");
            });
            query.append("}\"}");

            Utils.log(query, Utils.ErrorTypes.INFO);

            URL url = new URL("https://graphql.anilist.co");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.addRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(query.toString().getBytes(StandardCharsets.UTF_8));
            os.close();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String result = org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8);

            in.close();
            conn.disconnect();

            JSONObject animeObject = new JSONObject(result);
            JSONObject animeData = animeObject.getJSONObject("data");

            Iterator<String> keys = animeData.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                if (animeData.get(key) instanceof JSONObject) {
                    JSONObject animeMedia = animeData.getJSONObject(key);
                    JSONObject airingSchedule = animeMedia.getJSONObject("airingSchedule");

                    String englishTitle = animeMedia.getJSONObject("title").getString("english");
                    String romajiTitle = animeMedia.getJSONObject("title").getString("romaji");

                    JSONArray nodes = airingSchedule.getJSONArray("nodes");

                    String nextEpisodeAiring;
                    int episode;

                    for (Object n : nodes) {
                        JSONObject node = (JSONObject) n;
                        long nextAiringLong = node.getLong("timeUntilAiring");
                        if (nextAiringLong > 0) {
                            if (nextAiringLong < 60 * 30) { //30m
                                episode = node.getInt("episode");
                                nextEpisodeAiring = Utils.timeConvert(nextAiringLong / 60, "long");
                                msg.append("__**").append(romajiTitle).append(" (").append(englishTitle.replace("\u203c", "!")).append(")**__\n");
                                msg.append("Episode: ").append(episode).append(" | In: ").append(nextEpisodeAiring).append("\n");
                            }
                            break;
                        }
                    }
                }
            }

            if (msg.isEmpty()) {
                Utils.log("Tried to send but is empty", Utils.ErrorTypes.WARNING);
            } else {
                msg.append("<@426722323798818818> <@202666531111436288>");
                Objects.requireNonNull(BotMain.bot.getTextChannelById(BotMain.config.get("channel_animealerts").toString())).sendMessage(msg.toString()).queue();
            }

        } catch (IOException e) {
            Utils.log(e, Utils.ErrorTypes.ERROR);
        }
    }

    private String getCharForNumber(int i) {
        return i > 0 && i < 27 ? String.valueOf((char) (i + 64)) : null;
    }

}
