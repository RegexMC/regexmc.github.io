package me.regexmc.jdaregexbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.util.Utils;

import java.util.Date;

public class ParseDateCommand extends Command {

    public ParseDateCommand() {
        this.name = "parsedate";
        this.cooldown = 5;
        this.ownerCommand = true;
        this.arguments = "<epoch>";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nConverts the epoch time to AEST formatted date";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
        this.aliases = new String[]{"epoch"};
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            if (Utils.isAdmin(event)) {
                String[] args = event.getArgs().split(" ");
                Date date = new Date(Long.parseLong(args[0]) * 1000);
                event.reply(date.toString());
            }
        }
    }
}

