package me.regexmc.jdaregexbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.regexmc.jdaregexbot.BotMain;
import me.regexmc.jdaregexbot.util.Cache;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ClearCacheCommand extends Command {
    private final EventWaiter waiter;

    public ClearCacheCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "clearcache";
        this.cooldown = 60;
        this.ownerCommand = true;
        this.arguments = "[-y/-generic]";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nClears player files";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            String[] args = Arrays.copyOfRange(event.getMessage().getContentRaw().split(" "), 1, event.getMessage().getContentRaw().split(" ").length);
            if (args.length > 0) if (args[0].equalsIgnoreCase("-y")) {
                int deleted = Cache.clearCache().size();
                event.reply("Deleted " + deleted + " files");
                return;
            }

            if (args.length > 0) if (args[0].equalsIgnoreCase("-generic")) {
                BotMain.builtClient.cleanCooldowns();
                BotMain.bot.cancelRequests();
                return;
            }


            event.reply("Are you sure?");

            waiter.waitForEvent(MessageReceivedEvent.class,
                    e -> e.getAuthor().equals(event.getAuthor())
                            && e.getChannel().equals(event.getChannel())
                            && !e.getMessage().equals(event.getMessage()),
                    e -> {
                        if (e.getMessage().getContentRaw().equals("yes")) {
                            int deleted = Cache.clearCache().size();
                            e.getChannel().sendMessage("Deleted " + deleted + " files").queue();
                        }
                    }, 10, TimeUnit.SECONDS, () -> event.reply("No response from " + event.getAuthor().getName()));
        }
    }
}