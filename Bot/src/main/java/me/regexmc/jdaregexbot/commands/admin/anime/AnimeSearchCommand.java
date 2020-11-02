package me.regexmc.jdaregexbot.commands.admin.anime;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class AnimeSearchCommand extends Command {

    public AnimeSearchCommand() {
        this.name = "animesearch";
        this.cooldown = 10;
        this.ownerCommand = true;
        this.arguments = "--tags <tags> --tags_exclude <excluded_tags> --genre <genres> --genre_exclude <genres_excluded> --title <title>";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nSearches anime based on genres and tags (use 'genres' or 'tags' for list). If a tag, genre or title has a space in it, replace with an underscore";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            String[] args = event.getArgs().split(" ");
            String sort = "Popularity Descending";
            boolean byTitle = false;

            //region tags and genres
            ArrayList<String> genres = new ArrayList<>(Arrays.asList("Action",
                    "Adventure",
                    "Comedy",
                    "Drama",
                    "Ecchi",
                    "Fantasy",
                    "Hentai",
                    "Horror",
                    "Mahou Shoujo",
                    "Mecha",
                    "Music",
                    "Mystery",
                    "Psychological",
                    "Romance",
                    "Sci-Fi",
                    "Slice of Life",
                    "Sports",
                    "Supernatural",
                    "Thriller"));

            ArrayList<String> tags = new ArrayList<>(Arrays.asList("4-koma",
                    "Achromatic",
                    "Achronological Order",
                    "Acting",
                    "Advertisement",
                    "Afterlife",
                    "Age Gap",
                    "Age Regression",
                    "Agender",
                    "Ahegao",
                    "Airsoft",
                    "Aliens",
                    "Alternate Universe",
                    "American Football",
                    "Amnesia",
                    "Amputation",
                    "Anachronism",
                    "Anal Sex",
                    "Animals",
                    "Anthology",
                    "Anti-Hero",
                    "Archery",
                    "Armpits",
                    "Artificial Intelligence",
                    "Asexual",
                    "Ashikoki",
                    "Asphyxiation",
                    "Assassins",
                    "Astronomy",
                    "Athletics",
                    "Augmented Reality",
                    "Autobiographical",
                    "Aviation",
                    "Badminton",
                    "Band",
                    "Bar",
                    "Baseball",
                    "Basketball",
                    "Battle Royale",
                    "Biographical",
                    "Bisexual",
                    "Blackmail",
                    "Body Horror",
                    "Body Swapping",
                    "Bondage",
                    "Boobjob",
                    "Boxing",
                    "Boys' Love",
                    "Bullying",
                    "Calligraphy",
                    "Card Battle",
                    "Cars",
                    "Centaur",
                    "CGI",
                    "Cheerleading",
                    "Chibi",
                    "Chimera",
                    "Chuunibyou",
                    "Circus",
                    "Classic Literature",
                    "College",
                    "Coming of Age",
                    "Conspiracy",
                    "Cosmic Horror",
                    "Cosplay",
                    "Crime",
                    "Crossdressing",
                    "Crossover",
                    "Cult",
                    "Cultivation",
                    "Cunnilingus",
                    "Cute Girls Doing Cute Things",
                    "Cyberpunk",
                    "Cycling",
                    "Dancing",
                    "Dark Skin",
                    "Death Game",
                    "Defloration",
                    "Delinquents",
                    "Demons",
                    "Denpa",
                    "Detective",
                    "Dinosaurs",
                    "Dissociative Identities",
                    "Dragons",
                    "Drawing",
                    "Drugs",
                    "Dullahan",
                    "Dungeon",
                    "Dystopian",
                    "Economics",
                    "Educational",
                    "Elf",
                    "Ensemble Cast",
                    "Environmental",
                    "Episodic",
                    "Ero Guro",
                    "Espionage",
                    "Facial",
                    "Fairy Tale",
                    "Family Life",
                    "Fashion",
                    "Feet",
                    "Fellatio",
                    "Female Protagonist",
                    "Firefighters",
                    "Fishing",
                    "Fitness",
                    "Flash",
                    "Flat Chest",
                    "Food",
                    "Football",
                    "Foreign",
                    "Fugitive",
                    "Full CGI",
                    "Full Color",
                    "Futanari",
                    "Gambling",
                    "Gangs",
                    "Gender Bending",
                    "Ghost",
                    "Go",
                    "Goblin",
                    "Gods",
                    "Golf",
                    "Gore",
                    "Guns",
                    "Gyaru",
                    "Handjob",
                    "Harem",
                    "Henshin",
                    "Hikikomori",
                    "Historical",
                    "Human Pet",
                    "Ice Skating",
                    "Idol",
                    "Incest",
                    "Irrumatio",
                    "Isekai",
                    "Iyashikei",
                    "Josei",
                    "Kaiju",
                    "Karuta",
                    "Kemonomimi",
                    "Kids",
                    "Kuudere",
                    "Lacrosse",
                    "Lactation",
                    "Language Barrier",
                    "Large Breasts",
                    "LGBTQ Issues",
                    "Lost Civilization",
                    "Love Triangle",
                    "Mafia",
                    "Magic",
                    "Mahjong",
                    "Maids",
                    "Male Protagonist",
                    "Martial Arts",
                    "Masturbation",
                    "Medicine",
                    "Memory Manipulation",
                    "Mermaid",
                    "Meta",
                    "MILF",
                    "Military",
                    "Monster Girl",
                    "Mopeds",
                    "Motorcycles",
                    "Musical",
                    "Mythology",
                    "Nakadashi",
                    "Nekomimi",
                    "Netorare",
                    "Netorase",
                    "Netori",
                    "Ninja",
                    "No Dialogue",
                    "Noir",
                    "Nudity",
                    "Nun",
                    "Office Lady",
                    "Oiran",
                    "Ojou-sama",
                    "Omegaverse",
                    "Otaku Culture",
                    "Outdoor",
                    "Parody",
                    "Philosophy",
                    "Photography",
                    "Pirates",
                    "Poker",
                    "Police",
                    "Politics",
                    "Post-Apocalyptic",
                    "POV",
                    "Pregnant",
                    "Primarily Adult Cast",
                    "Primarily Child Cast",
                    "Primarily Female Cast",
                    "Primarily Male Cast",
                    "Prostitution",
                    "Public Sex",
                    "Puppetry",
                    "Rakugo",
                    "Rape",
                    "Real Robot",
                    "Rehabilitation",
                    "Reincarnation",
                    "Revenge",
                    "Reverse Harem",
                    "Robots",
                    "Rotoscoping",
                    "Rugby",
                    "Rural",
                    "Sadism",
                    "Samurai",
                    "Satire",
                    "Scat",
                    "School",
                    "School Club",
                    "Seinen",
                    "Sex Toys",
                    "Shapeshifting",
                    "Ships",
                    "Shogi",
                    "Shoujo",
                    "Shounen",
                    "Shrine Maiden",
                    "Skeleton",
                    "Slapstick",
                    "Slavery",
                    "Software Development",
                    "Space",
                    "Space Opera",
                    "Steampunk",
                    "Stop Motion",
                    "Succubus",
                    "Sumata",
                    "Super Power",
                    "Super Robot",
                    "Superhero",
                    "Surfing",
                    "Surreal Comedy",
                    "Survival",
                    "Sweat",
                    "Swimming",
                    "Swordplay",
                    "Table Tennis",
                    "Tanks",
                    "Teacher",
                    "Teens' Love",
                    "Tennis",
                    "Tentacles",
                    "Terrorism",
                    "Threesome",
                    "Time Manipulation",
                    "Time Skip",
                    "Tokusatsu",
                    "Tragedy",
                    "Trains",
                    "Triads",
                    "Tsundere",
                    "Twins",
                    "Urban",
                    "Urban Fantasy",
                    "Urination",
                    "Vampire",
                    "Video Games",
                    "Vikings",
                    "Virginity",
                    "Virtual World",
                    "Volleyball",
                    "Vore",
                    "Voyeur",
                    "War",
                    "Werewolf",
                    "Witch",
                    "Work",
                    "Wrestling",
                    "Writing",
                    "Wuxia",
                    "Yakuza",
                    "Yandere",
                    "Youkai",
                    "Yuri",
                    "Zombie"));
            //endregion

            if (args.length < 1) {
                event.reply("Syntax: `!!animesearch --tags <tags> --tags_exclude <excluded_tags> --genre <genres> --genre_exclude <genres_excluded>`\nIf a tag or genre has a space in it, replace with an underscore");
            } else {
                if (args[0].equalsIgnoreCase("tags")) {
                    EmbedBuilder temp = new EmbedBuilder();
                    temp.setTitle("List of tags");
                    temp.addField("Link", "[CLICK ME](https://gist.githubusercontent.com/RegexMC/6574c7fb5d46eb9fbdd4c0a8ed89ce52/raw/tags.txt)", true);
                    event.reply(temp.build());
                    return;
                } else if (args[0].equalsIgnoreCase("genres")) {
                    StringBuilder genresVal = new StringBuilder();
                    genres.forEach(g -> genresVal.append(g).append(", "));

                    EmbedBuilder temp = new EmbedBuilder();
                    temp.setTitle("List of genres");
                    temp.addField("Genres", genresVal.toString().trim().substring(0, genresVal.toString().trim().length() - 1), true);
                    event.reply(temp.build());
                    return;
                }

                EmbedBuilder embed = new EmbedBuilder();

                try {
                    String queryFormat = Utils.loadResourceAsString("anilist_searchquery");

                    ArrayList<String> flags = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(event.getMessage().getContentRaw().split("--"), 1, event.getMessage().getContentRaw().split("--").length)));

                    for (String s : flags) {
                        String flagName = s.split(" ")[0];
                        ArrayList<String> flagModifiers = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(s.split(" "), 1, s.split(" ").length)));

                        for (int i = 0; i < flagModifiers.size(); i++) {
                            flagModifiers.set(i, WordUtils.capitalize(flagModifiers.get(i).replaceAll(",", "").replaceAll(" ", "").toLowerCase()));
                        }

                        if (flagModifiers.size() != 0) {
                            switch (flagName.toLowerCase()) {
                                case "sort" -> {
                                    switch (flagModifiers.get(0).toLowerCase()) {
                                        case "score" -> {
                                            queryFormat = queryFormat.replace("%%sort%%", "SCORE_DESC");
                                            sort = "Score Descending";
                                        }
                                        case "favourites" -> {
                                            queryFormat = queryFormat.replace("%%sort%%", "FAVOURITES_DESC");
                                            sort = "Favourites";
                                        }
                                        default -> queryFormat = queryFormat.replace("%%sort%%", "POPULARITY_DESC");
                                    }
                                }
                                case "tags" -> {
                                    if (Collections.disjoint(flagModifiers, tags)) {
                                        event.reply("Invalid tag(s)");
                                        return;
                                    }
                                    queryFormat = queryFormat.replace("%%tagin%%", "[" + flagModifiers.stream().collect(Collectors.joining("\\\", \\\"", "\\\"", "\\\"")) + "]");
                                }
                                case "tags_exclude" -> {
                                    if (Collections.disjoint(flagModifiers, tags)) {
                                        event.reply("Invalid tag(s)");
                                        return;
                                    }
                                    queryFormat = queryFormat.replace("%%tagnotin%%", "[" + flagModifiers.stream().collect(Collectors.joining("\\\", \\\"", "\\\"", "\\\"")) + "]");
                                }
                                case "genre" -> {
                                    if (Collections.disjoint(flagModifiers, genres)) {
                                        event.reply("Invalid genre(s)");
                                        return;
                                    }
                                    queryFormat = queryFormat.replace("%%genrein%%", "[" + flagModifiers.stream().collect(Collectors.joining("\\\", \\\"", "\\\"", "\\\"")) + "]");
                                }
                                case "genre_exclude" -> {
                                    if (Collections.disjoint(flagModifiers, genres)) {
                                        event.reply("Invalid genre(s)");
                                        return;
                                    }
                                    queryFormat = queryFormat.replace("%%genrenotin%%", "[" + flagModifiers.stream().collect(Collectors.joining("\\\", \\\"", "\\\"", "\\\"")) + "]");
                                }
                                case "title" -> {
                                    byTitle = true;
                                    queryFormat = "{Page(page: 1, perPage: 5) {media(search:\\\"" + flagModifiers.get(0) + "\\\", sort:POPULARITY_DESC, type: ANIME) {title {english romaji} averageScore popularity description siteUrl}}}";
                                }
                                case "limit" -> queryFormat = queryFormat.replace("perPage: 5", "perPage: " + flagModifiers.get(0));
                            }
                        }
                    }

                    queryFormat = queryFormat.replace("tag_not_in: %%tagnotin%%,", "")
                            .replace("tag_in: %%tagin%%,", "")
                            .replace("genre_not_in:%%genrenotin%%,", "")
                            .replace("genre_in: %%genrein%%,", "")
                            .replace("%%sort%%", "POPULARITY_DESC");

                    URL url = new URL("https://graphql.anilist.co");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.addRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");

                    OutputStream os = conn.getOutputStream();
                    os.write(("{\"query\":\"" + queryFormat + "\"}").replaceAll(System.getProperty("line.separator"), " ").getBytes(StandardCharsets.UTF_8));
                    os.close();

                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    String result = org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8);

                    in.close();
                    conn.disconnect();

                    JSONObject results = new JSONObject(result);

                    JSONArray items = results.getJSONObject("data").getJSONObject("Page").getJSONArray("media");

                    int maxCharsInDesc = 800;
                    for (int i = 0; i < 16; i++) {
                        if (byTitle) {
                            embed.setTitle("Most popular anime matching search");

                            int finalMaxCharsInDesc = maxCharsInDesc;
                            items.forEach(o -> {
                                JSONObject animeObject = (JSONObject) o;

                                String englishTitle = animeObject.getJSONObject("title").isNull("english") ? animeObject.getJSONObject("title").getString("romaji") : animeObject.getJSONObject("title").getString("english");
                                int averageScore = animeObject.isNull("averageScore") ? 0 : animeObject.getInt("averageScore");
                                int popularity = animeObject.getInt("popularity");
                                String siteUrl = animeObject.getString("siteUrl");
                                String description = animeObject.isNull("description") ? "" : Jsoup.parse(animeObject.getString("description")).text();

                                if (description.length() > finalMaxCharsInDesc)
                                    description = description.substring(0, finalMaxCharsInDesc - 3) + "...";

                                String value = "[Link](" + siteUrl + " '" + siteUrl + "')\n" +
                                        "Score: " + averageScore + "\n" +
                                        "Popularity: " + popularity + "\n" +
                                        "Description: `" + description + "`";
                                embed.addField(englishTitle, value, false);
                            });
                        } else {
                            embed.setTitle("Anime rankings based on query");
                            embed.setFooter("Sorted by " + sort);
                            items.forEach(o -> {
                                JSONObject animeObject = (JSONObject) o;

                                String englishTitle = animeObject.getJSONObject("title").isNull("english") ? animeObject.getJSONObject("title").getString("romaji") : animeObject.getJSONObject("title").getString("english");
                                int averageScore = animeObject.getInt("averageScore");
                                int popularity = animeObject.getInt("popularity");
                                int favourites = animeObject.getInt("favourites");
                                String siteUrl = animeObject.getString("siteUrl");

                                String value = "[Link](" + siteUrl + " '" + siteUrl + "')\n" +
                                        "Score: " + averageScore + "\n" +
                                        "Popularity: " + popularity + "\n" +
                                        "Favourites: " + favourites + "\n";
                                embed.addField(englishTitle, value, false);
                            });
                        }

                        if (embed.length() > 6000) {
                            maxCharsInDesc = 500 - (i * 50);
                            embed.clearFields();
                        } else {
                            break;
                        }
                    }

                    if (embed.getFields().size() == 0) embed.addBlankField(false);
                    event.reply(embed.build());
                } catch (IOException e) {
                    Utils.log(e, Utils.ErrorTypes.ERROR);
                    e.printStackTrace();
                    event.reply("Something went wrong. (Invalid parameters?)");
                }
            }
        }
    }
}
