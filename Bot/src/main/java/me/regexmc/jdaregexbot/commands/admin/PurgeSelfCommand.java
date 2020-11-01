package me.regexmc.jdaregexbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.BotMain;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class PurgeSelfCommand extends Command {

    public PurgeSelfCommand() {
        this.name = "purgeself";
        this.cooldown = 60;
        this.ownerCommand = true;
        this.arguments = "[messages to scan]";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nDeletes bots’ own messages within the last [messages]";
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
            int amountToScan = 100;
            if (args.length > 0) {
                if (Utils.isNumeric(args[0])) amountToScan = Utils.parseInt(args[0]);
                if (amountToScan > 1000) amountToScan = 1000;
            }
            CompletableFuture<List<Message>> messageList = event.getChannel().getIterableHistory().takeAsync(amountToScan)
                    .thenApply(list ->
                            list.stream()
                                    .filter(m -> m.getAuthor().getId().equals(BotMain.bot.getSelfUser().getId()))
                                    .limit(100)
                                    .collect(Collectors.toList())
                    );
            try {
                List<Message> messages = messageList.get();
                if (messages.size() < 2) {
                    messages.forEach(m -> m.delete().queue());
                } else {
                    Objects.requireNonNull(event.getGuild().getTextChannelById(event.getChannel().getId())).deleteMessages(messages).queue();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        }
    }
}
