package me.regexmc.jdaregexbot.commands.admin.anime;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class RandomHentaiCommand extends Command {

    public RandomHentaiCommand() {
        this.name = "randomhentai";
        this.cooldown = 5;
        this.ownerCommand = true;
        this.arguments = "[--exclude_tags tags]";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nGets a random doujinshi that excludes any tag provided";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            int randomNum = ThreadLocalRandom.current().nextInt(1, 328000 + 1);

            String url = "https://nhentai.net/g/" + randomNum;

            EmbedBuilder embed = new EmbedBuilder();

            AtomicReference<String> pages = new AtomicReference<>("");

            try {
                org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
                embed.setTitle(doc.title(), doc.location());
                Elements hrefs = doc.select("a[href]");
                StringBuilder tags = new StringBuilder();
                tags.append("`");
                String favourites = doc.select("a .nobold").text().replace("(", "").replace(")", "");
                hrefs.forEach(t -> {
                    if (t.attr("href").startsWith("/tag/")) {
                        String tag = t.attr("href").substring(5);
                        tag = tag.substring(0, tag.length() - 1);
                        tags.append(tag).append(", ");
                    } else if (t.attr("href").startsWith("/search/?q=pages")) {
                        Element child = t.child(0);

                        pages.set(child.text());

                        System.out.println(child);
                    }
                });

                tags.append("`");
                embed.addField("Favourites", favourites, false);
                embed.addField("Pages", String.valueOf(pages), false);
                embed.addField("Tags", tags.toString().replace(", `", "`"), false);

            } catch (IOException e) {
                e.printStackTrace();
                event.reply("Error");
                return;
            }

            event.reply(embed.build());
        }
    }
}