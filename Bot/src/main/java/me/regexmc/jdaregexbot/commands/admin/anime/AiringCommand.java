package me.regexmc.jdaregexbot.commands.admin.anime;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.regexmc.jdaregexbot.util.PageHandler;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AiringCommand extends Command {

    final EventWaiter waiter;

    public AiringCommand(EventWaiter waiter) {
        this.name = "airing";
        this.cooldown = 30;
        this.arguments = "[latest | next x hours]";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nLists all airing anime that devs are watching";
        this.ownerCommand = true;
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
        this.waiter = waiter;
        this.category = Utils.CommandCategories.ANIME.getCategory();
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            boolean isLatestReleases = false;
            boolean isNextXHours = false;
            int xHours = 0;

            List<EmbedBuilder> embedBuilders = new ArrayList<>();
            embedBuilders.add(new EmbedBuilder());

            try {
                String[] args = event.getArgs().split(" ");
                if (args.length > 0) {
                    String arg = args[0].replace("h", "").replace("d", "").replace("m", "");
                    isLatestReleases = arg.equalsIgnoreCase("latest");
                    embedBuilders.set(0, embedBuilders.get(0).setTitle(isLatestReleases ? "Releases in past 24h" : "Airing Anime"));
                    isNextXHours = Utils.isNumeric(arg);
                    if (isNextXHours)
                        xHours = Utils.parseInt(arg) > (7 * 24 * 3600) ? 604800 : Utils.parseInt(arg);
                }

               // List<Object> animeIds = new JSONObject(JsonParser.parseString(Utils.loadResourceAsString("anime.json")).getAsJsonObject().toString())
               //         .getJSONArray("ids").toList();
                List<Object> animeIds = new JSONObject(Utils.readJsonFromFile(Utils.loadResourceAsString("anime.json"))).getJSONArray("ids").toList();

                StringBuilder query = new StringBuilder();
                query.append("{\"query\":\"{");

                AtomicInteger c = new AtomicInteger(1);

                animeIds.forEach(o -> {
                    c.getAndIncrement();
                    query.append(getCharForNumber(c.get() - 1)).append(":Media(id:").append(o).append("){title{romaji english}airingSchedule{nodes{episode airingAt timeUntilAiring}}}");
                });
                query.append("}\"}");

                Utils.log(query.toString(), Utils.ErrorTypes.INFO);

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

                TreeMap<Long, JSONObject> animes = new TreeMap<>();

                while (keys.hasNext()) {
                    String key = keys.next();
                    if (animeData.get(key) instanceof JSONObject) {
                        JSONObject animeMedia = animeData.getJSONObject(key);
                        JSONObject airingSchedule = animeMedia.getJSONObject("airingSchedule");

                        JSONArray nodes = airingSchedule.getJSONArray("nodes");

                        for (Object n : nodes) {
                            JSONObject node = (JSONObject) n;
                            long nextAiringLong = node.getLong("timeUntilAiring");
                            if (isLatestReleases ? (nextAiringLong < 0 && nextAiringLong > -86400) : (isNextXHours ? (nextAiringLong > 0 && nextAiringLong < xHours * 3600) : nextAiringLong > 0)) {
                                animes.put(nextAiringLong, animeMedia);
                                break;
                            }
                        }
                    }
                }

                for (Map.Entry<Long, JSONObject> entry : animes.entrySet()) {
                    JSONObject animeMedia = entry.getValue();
                    JSONObject airingSchedule = animeMedia.getJSONObject("airingSchedule");

                    String englishTitle = animeMedia.getJSONObject("title").optString("english");
                    String romajiTitle = animeMedia.getJSONObject("title").optString("romaji");

                    String nextEpisodeAiring = "";
                    int episode = 0;
                    String exactDate = "";

                    JSONArray nodes = airingSchedule.getJSONArray("nodes");
                    for (Object n : nodes) {
                        JSONObject node = (JSONObject) n;
                        long nextAiringLong = node.getLong("timeUntilAiring");
                        if (isLatestReleases ? (nextAiringLong < 0 && nextAiringLong > -86400) : (isNextXHours ? (nextAiringLong > 0 && nextAiringLong < xHours * 3600) : nextAiringLong > 0)) {
                            episode = node.getInt("episode");
                            exactDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(node.getLong("airingAt") * 1000));
                            nextEpisodeAiring = Utils.timeConvert(nextAiringLong / 60, isLatestReleases ? "hours" : "long");
                            break;
                        }
                    }

                    if (embedBuilders.get(embedBuilders.size() - 1).getFields().size() > 4)
                        embedBuilders.add(new EmbedBuilder().setTitle(isLatestReleases ? "Releases in past 24h" : "Airing Anime"));


                    if (isLatestReleases) {
                        embedBuilders.set(embedBuilders.size() - 1, embedBuilders.get(embedBuilders.size() - 1).addField(romajiTitle + " (" + englishTitle.replace("\u203c", "!") + ")", "Episode: " + episode + " | Aired " + nextEpisodeAiring.replace("-", "") + " hours ago", false));
                    } else {
                        embedBuilders.set(embedBuilders.size() - 1, embedBuilders.get(embedBuilders.size() - 1).addField(romajiTitle + " (" + englishTitle.replace("\u203c", "!") + ")", "Episode: " + episode + " | Airs At: " + exactDate + " | In: " + nextEpisodeAiring, false));
                    }
                }
            } catch (IOException e) {
                Utils.log(e, Utils.ErrorTypes.ERROR);
                e.printStackTrace();
            }

            for (int i = 0; i < embedBuilders.size(); i++) {
                if (embedBuilders.get(i).getFields().size() == 0)
                    embedBuilders.set(i, embedBuilders.get(i).addBlankField(false));
            }

            embedBuilders.set(0, embedBuilders.get(0).setFooter("Page 1 of " + embedBuilders.size()));

            event.getChannel().sendMessage(embedBuilders.get(0).build()).queue(msg -> {
                EmbedBuilder[] embeds = new EmbedBuilder[embedBuilders.size()];
                embedBuilders.toArray(embeds);
                PageHandler.managePages(waiter, event.getAuthor(), msg, embeds, 0, true);
            });
        } else {
            event.reply("Not a command channel");
        }
    }

    private String getCharForNumber(int i) {
        return i > 0 && i < 27 ? String.valueOf((char) (i + 64)) : null;
    }
}