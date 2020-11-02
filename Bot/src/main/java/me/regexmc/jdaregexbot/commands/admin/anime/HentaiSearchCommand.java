package me.regexmc.jdaregexbot.commands.admin.anime;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class HentaiSearchCommand extends Command {

    public HentaiSearchCommand() {
        this.name = "hentaisearch";
        this.cooldown = 5;
        this.ownerCommand = true;
        this.arguments = "[--tags tags --exclude_tags tags]";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nGets the top x doujinshis that matches input tags";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            boolean isId = false;

            String query;
            String id = "";
            String sort = "Recent";

            EmbedBuilder embed = new EmbedBuilder();

            ArrayList<String> flags = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(event.getMessage().getContentRaw().split("--"), 1, event.getMessage().getContentRaw().split("--").length)));
            AtomicReference<String> tagQuery = new AtomicReference<>("");
            AtomicReference<String> sortQuery = new AtomicReference<>("");

            for (String s : flags) {
                String flagName = s.split(" ")[0];
                ArrayList<String> flagModifiers = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(s.split(" "), 1, s.split(" ").length)));

                if (flagModifiers.size() != 0) {
                    switch (flagName.toLowerCase()) {
                        case "sort" -> {
                            switch (flagModifiers.get(0).toLowerCase()) {
                                case "all_time" -> {
                                    sortQuery.set("&sort=popular");
                                    sort = "All Time";
                                }
                                case "weekly" -> {
                                    sortQuery.set("&sort=popular-week");
                                    sort = "Weekly";
                                }
                                case "daily" -> {
                                    sortQuery.set("&sort=popular-today");
                                    sort = "Today";
                                }
                            }
                        }
                        case "tags" -> flagModifiers.forEach(f -> tagQuery.set(tagQuery.get() + "+" + (f.contains("_") ? "\"" + f.replace("_", " ") + "\"" : f)));
                        case "exclude_tags" -> flagModifiers.forEach(f -> tagQuery.set(tagQuery.get() + "-" + (f.contains("_") ? "\"" + f.replace("_", " ") + "\"" : f)));
                        case "id" -> {
                            isId = true;
                            id = Utils.isNumeric(flagModifiers.get(0)) ? flagModifiers.get(0) : null;
                        }
                    }
                }
            }

            query = tagQuery.get();

            try {
                String formattedQuery;

                /*
                if (isId) {
                    if (id == null) {
                        event.reply("Invalid ID provided");
                    } else {
                        formattedQuery = "https://nhentai.net/g/" + id;
                        Document doc = Jsoup.connect(formattedQuery).get();
                        AtomicReference<String> pages = new AtomicReference<>("");

                        Elements hrefs = doc.select("a[href]");
                        StringBuilder tags = new StringBuilder();
                        tags.append("`");
                        String title = doc.title();
                        String favourites = doc.select("a .nobold").text().replace("(", "").replace(")", "");
                        hrefs.forEach(t -> {
                            if (t.attr("href").startsWith("/tag/")) {
                                String tag = t.attr("href").substring(5);
                                tag = tag.substring(0, tag.length() - 1);
                                tags.append(tag).append(", ");
                            } else if (t.attr("href").startsWith("/search/?q=pages")) {
                                Element child = t.child(0);
                                pages.set(child.text());
                            }
                        });

                        tags.append("`");
                        embed.setTitle(title, formattedQuery);
                        embed.addField("Favourites", favourites, false);
                        embed.addField("Pages", String.valueOf(pages), false);
                        embed.addField("Tags", tags.toString().replace(", `", "`"), false);
                        event.reply(embed.build());
                    }
                } else {
                    formattedQuery = "https://nhentai.net/search/?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + sortQuery;
                    Document doc = Jsoup.connect(formattedQuery).get();

                    Elements elements = doc.select(".caption");
                    elements.forEach(e -> {
                        String title = e.text();
                        String numbers = e.parent().attr("href");
                        String link = "https://nhentai.net" + numbers;
                        embed.addField("**" + title + "**", "[" + numbers.substring(3).replace("/", "") + "](" + link + ")", false);
                    });

                    embed.setTitle("Top 25 Doujinshis matching input");
                    embed.setFooter(sort);
                    event.reply(embed.build());
                }
                 */

                if (id == null) {
                    event.reply("Invalid ID provided");
                    return;
                }
                formattedQuery = isId ? "https://nhentai.net/g/" + id : "https://nhentai.net/search/?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + sortQuery;
                Document doc = Jsoup.connect(formattedQuery).get();

                if (isId) {
                    AtomicReference<String> pages = new AtomicReference<>("");

                    Elements hrefs = doc.select("a[href]");
                    StringBuilder tags = new StringBuilder();
                    tags.append("`");
                    String title = doc.title();
                    String favourites = doc.select("a .nobold").text().replace("(", "").replace(")", "");
                    hrefs.forEach(t -> {
                        if (t.attr("href").startsWith("/tag/")) {
                            String tag = t.attr("href").substring(5);
                            tag = tag.substring(0, tag.length() - 1);
                            tags.append(tag).append(", ");
                        } else if (t.attr("href").startsWith("/search/?q=pages")) {
                            Element child = t.child(0);
                            pages.set(child.text());
                        }
                    });

                    tags.append("`");
                    embed.setTitle(title, formattedQuery);
                    embed.addField("Favourites", favourites, false);
                    embed.addField("Pages", String.valueOf(pages), false);
                    embed.addField("Tags", tags.toString().replace(", `", "`"), false);

                } else {
                    Elements elements = doc.select(".caption");
                    elements.forEach(e -> {
                        String title = e.text();
                        String numbers = e.parent().attr("href");
                        String link = "https://nhentai.net" + numbers;
                        embed.addField("**" + title + "**", "[" + numbers.substring(3).replace("/", "") + "](" + link + ")", false);
                    });

                    embed.setTitle("Top 25 Doujinshis matching input");
                    embed.setFooter(sort);
                }
                event.reply(embed.build());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
