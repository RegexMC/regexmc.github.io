package me.regexmc.jdaregexbot.commands.admin.anime;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
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

public class AnilistCommand extends Command {

    public AnilistCommand() {
        this.name = "anilist";
        this.cooldown = 5;
        this.ownerCommand = true;
        this.arguments = "<username>";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nGets the anilist profile of <username>";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
    }

    private static Color getColorFromString(String color) {
        return switch (color) {
            case "blue" -> Color.decode("#3DB4F2");
            case "purple" -> Color.decode("#C063FF");
            case "green" -> Color.decode("#4CCA51");
            case "orange" -> Color.decode("#EF881A");
            case "red" -> Color.decode("#E13333");
            case "pink" -> Color.decode("#FC9DD6");
            default -> Color.decode("#677B94"); //gray
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            String[] args = event.getArgs().split(" ");

            if (args.length < 1) {
                event.reply("Include a username");
                return;
            }
            try {
                String json = ("{\"query\":\"" + Utils.loadResourceAsString("anilist_userquery") + "\"}").replace("%%name%%", args[0] + "").replaceAll(System.getProperty("line.separator"), " ");
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

                JSONObject userData = new JSONObject(result).getJSONObject("data");
                JSONObject page = userData.getJSONObject("Page");
                JSONArray mediaList = page.getJSONArray("mediaList");

                JSONObject user = mediaList.getJSONObject(0).getJSONObject("user");
                JSONObject statistics = user.getJSONObject("statistics");
                JSONObject favourites = user.getJSONObject("favourites").getJSONObject("anime");

                String name = user.getString("name");
                String about = user.isNull("about") ? "About" : user.getString("about");
                String avatarURL = user.getJSONObject("avatar").optString("large");
                String bannerURL = user.optString("bannerImage");
                String userURL = user.getString("siteUrl");
                String userColor = user.getJSONObject("options").getString("profileColor");

                JSONObject recentActivity = mediaList.getJSONObject(0);
                JSONObject recentMedia = recentActivity.getJSONObject("media");
                String recentType = recentMedia.getString("type");
                String recentEnglishTitle = recentMedia.getJSONObject("title").optString("english");
                String recentRomajiTitle = recentMedia.getJSONObject("title").optString("romaji");
                int progress = recentActivity.optInt("progress");

                long animeWatchTime = statistics.getJSONObject("anime").getLong("minutesWatched");
                int entries = statistics.getJSONObject("anime").getInt("count");

                JSONArray nodes = favourites.getJSONArray("nodes");
                int favouriteCount = nodes.length();

                EmbedBuilder userEmbed = new EmbedBuilder();
                userEmbed.setTitle(name, userURL);
                userEmbed.setColor(getColorFromString(userColor));
                if (bannerURL.endsWith(".png") || bannerURL.endsWith(".jpg") || bannerURL.endsWith("jpeg"))
                    userEmbed.setImage(bannerURL);
                if (avatarURL.endsWith(".png") || avatarURL.endsWith(".jpg") || avatarURL.endsWith("jpeg"))
                    userEmbed.setThumbnail(avatarURL);
                userEmbed.setDescription(about);
                userEmbed.setFooter(userURL);

                userEmbed.addField("Watchtime", Utils.timeConvert(animeWatchTime, "default"), false);
                userEmbed.addField("Entries", entries + "", false);
                userEmbed.addField("Favourites", favouriteCount + "", false);
                userEmbed.addField("Recent Activity", (recentType.equals("ANIME") ? "Watched `" : "Read `") + recentRomajiTitle + " (" + recentEnglishTitle + ")` [" + progress + "]", false);

                event.reply(userEmbed.build());

                in.close();
                conn.disconnect();
            } catch (IOException | JSONException | IllegalArgumentException e) {
                Utils.log(e, Utils.ErrorTypes.ERROR);
                e.printStackTrace();
                event.reply("Something went wrong. (Invalid User?)");
            }
        }
    }
}
