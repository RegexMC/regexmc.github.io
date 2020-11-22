package me.regexmc.jdaregexbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.regexmc.jdaregexbot.BotMain;
import me.regexmc.jdaregexbot.util.PageHandler;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.LinkedHashMap;

public class HelpCommand extends Command {
    private final EventWaiter waiter;

    public HelpCommand(EventWaiter waiter) {
        this.name = "help";
        this.help = "List commands";
        this.cooldown = 10;
        this.category = Utils.CommandCategories.GENERIC.getCategory();
        this.waiter = waiter;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            LinkedHashMap<Category, EmbedBuilder> embedBuilderHashMap = new LinkedHashMap<>();

            BotMain.builtClient.getCommands().forEach(command -> {
                if (command.isOwnerCommand() && !Utils.isAdmin(event)) return;

                Category category = command.getCategory();

                if (embedBuilderHashMap.containsKey(category)) {
                    EmbedBuilder embed = embedBuilderHashMap.get(category);
                    embed.addField(command.getName(), command.getHelp(), false);
                    embedBuilderHashMap.replace(category, embed);
                } else {
                    embedBuilderHashMap.put(category, new EmbedBuilder().setTitle(category.getName()).addField(command.getName(), command.getHelp(), false));
                }
            });
            EmbedBuilder[] embedBuilderArrayList = new EmbedBuilder[embedBuilderHashMap.values().size()];
            embedBuilderHashMap.values().toArray(embedBuilderArrayList);
            event.getChannel().sendMessage(embedBuilderArrayList[0].build()).queue(msg -> PageHandler.managePages(waiter, event.getAuthor(), msg, embedBuilderArrayList, 0, true));
        }
    }
}