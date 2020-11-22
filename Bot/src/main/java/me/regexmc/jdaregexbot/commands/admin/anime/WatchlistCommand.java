package me.regexmc.jdaregexbot.commands.admin.anime;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.regexmc.jdaregexbot.util.PageHandler;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WatchlistCommand extends Command {
    final EventWaiter waiter;

    public WatchlistCommand(EventWaiter waiter) {
        this.name = "watchlist";
        this.cooldown = 5;
        this.ownerCommand = true;
        this.arguments = "<username> [planning|watching|completed|paused|dropped|repeating]";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nGets the watchlist profile of <username>";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
        this.waiter = waiter;
        this.category = Utils.CommandCategories.ANIME.getCategory();
    }

    private static Color getColorFromString(String color) {
        return switch (color) {
            case "PLANNING" -> Color.decode("#02a9ff");
            case "CURRENT" -> Color.decode("#f779a4");
            case "COMPLETED" -> Color.decode("#68d639");
            case "PAUSED" -> Color.decode("#e85d75");
            case "DROPPED" -> Color.decode("#9256f3");
            case "REPEATING" -> Color.decode("#701c5a");
            default -> Color.decode("#677B94");
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            String[] args = event.getArgs().split(" ");
            ArrayList<EmbedBuilder> embeds = new ArrayList<>();

            if (args.length < 1) {
                event.reply("Include a username");
                return;
            }
            try {
                String status = args.length > 1 ? args[1] : "completed";

                switch (status.toLowerCase()) {
                    case "planning", "ptw" -> status = "PLANNING";
                    case "watching" -> status = "CURRENT";
                    case "completed" -> status = "COMPLETED";
                    case "paused" -> status = "PAUSED";
                    case "dropped" -> status = "DROPPED";
                    case "repeating" -> status = "REPEATING";
                }

                boolean hasNextPage = true;
                int page = 1;

                //noinspection ConstantConditions
                while (hasNextPage) {
                    String json = ("{\"query\":\"" + Utils.loadResourceAsString("anilist_userquery") + "\"}")
                            .replace("%%name%%", args[0] + "")
                            .replace("%%page%%", page + "")
                            .replace("%%status%%", status)
                            .replaceAll(System.getProperty("line.separator"), " ");

                    URL url = new URL("https://graphql.anilist.co");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.addRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");

                    OutputStream os = conn.getOutputStream();
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                    os.close();

                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    String result = org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8);

                    in.close();
                    conn.disconnect();

                    JSONObject userData = new JSONObject(result).getJSONObject("data");
                    boolean nextPage = userData.getJSONObject("b").getJSONObject("pageInfo").getBoolean("hasNextPage");
                    JSONArray mediaList = userData.getJSONObject("b").getJSONArray("mediaList");

                    JSONObject user = userData.getJSONObject("User");
                    String name = user.getString("name");

                    String finalStatus = status;
                    mediaList.forEach(m -> {
                        JSONObject media = ((JSONObject) m).getJSONObject("media");
                        JSONObject titles = media.getJSONObject("title");
                        String englishTitle = titles.optString("english");
                        String romajiTitle = titles.optString("romaji");

                        String type = media.optString("type");
                        int averageScore = media.optInt("averageScore");

                        List<Object> genres = media.optJSONArray("genres").toList();
                        StringBuilder genreString = new StringBuilder();
                        for (int i = 0; i < genres.size(); i++) {
                            if (i == genres.size() - 1) {
                                genreString.append(genres.get(i));
                            } else {
                                genreString.append(genres.get(i)).append(", ");
                            }
                        }

                        String value = type + " | Score: " + averageScore + " | Genres: `" + genreString.toString() + "`";

                        int size = embeds.size() > 0 ? embeds.get(embeds.size() - 1).getFields().size() : 0;

                        if (size == 0 || embeds.get(embeds.size() - 1).getFields().size() > 4) {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setColor(getColorFromString(finalStatus));
                            embed.setTitle(name + "'s anime list");
                            embed.addField(englishTitle.isEmpty() ? romajiTitle : englishTitle, value, false);
                            embeds.add(embed);
                        } else if (embeds.get(embeds.size() - 1).getFields().size() < 5) {
                            EmbedBuilder embed = embeds.get(embeds.size() - 1);
                            embed.addField(englishTitle.isEmpty() ? romajiTitle : englishTitle, value, false);
                            embeds.set(embeds.size() - 1, embed);
                        }
                    });

                    hasNextPage = nextPage;
                    page++;
                    if (!hasNextPage) break;
                }

                embeds.set(0, embeds.get(0).setFooter("Page 1 of " + embeds.size()));
                event.getChannel().sendMessage(embeds.get(0).build()).queue(msg -> {
                    EmbedBuilder[] embedArray = new EmbedBuilder[embeds.size()];
                    embeds.toArray(embedArray);
                    PageHandler.managePages(waiter, event.getAuthor(), msg, embedArray, 0, true);
                });
            } catch (IOException | JSONException | IllegalArgumentException e) {
                Utils.log(e, Utils.ErrorTypes.ERROR);
                e.printStackTrace();
                event.reply("Something went wrong. (Invalid User?)");
            }
        }
    }
}
