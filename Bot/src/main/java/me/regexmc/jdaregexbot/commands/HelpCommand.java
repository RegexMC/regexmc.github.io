package me.regexmc.jdaregexbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.util.RankUtils;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

public class HelpCommand extends Command {
    public HelpCommand() {
        this.name = "help";
        this.help = "List commands";
        this.cooldown = 10;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            EmbedBuilder helpEmbed = new EmbedBuilder();

            helpEmbed.setTitle("Commands", "https://regexmc.github.io/");
            helpEmbed.addField("Hypixel Commands", """
                    stats <ign> - Get the players general hypixel stats
                    skywars <ign> - Gets the players skywars stats
                    ranked <ign> - Gets the players ranked positions (does not get current season)
                    bedwars <ign> - Get the players bedwars stats""", true);

            if (Utils.isAdmin(event)) {
                helpEmbed.addField("Admin Commands", """
                        airing [latest / hours into future]
                        anilist <username>
                        animesearch [help]
                        hentaisearch [help]
                        randomhentai
                        compressimage
                        charcount <input>
                        clearcache [-y]
                        convert [help]
                        eval <js>
                        parsedate <epoch>
                        timezone""", false);
            }

            helpEmbed.setFooter("@RegexMC");
            helpEmbed.setColor(RankUtils.Ranks.ADMIN.getColor());

            event.reply(helpEmbed.build());
        }
    }
}