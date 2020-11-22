package me.regexmc.jdaregexbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.util.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimezoneCommand extends Command {

    public TimezoneCommand() {
        this.name = "timezone";
        this.cooldown = 5;
        this.ownerCommand = true;
        this.arguments = "<timezone>";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nGets current time in <timezone>";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
        this.aliases = new String[]{"tz"};
        this.category = Utils.CommandCategories.GENERIC.getCategory();
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            if (Utils.isAdmin(event)) {
                String[] args = event.getArgs().split(" ");
                if (args.length < 1) {
                    event.reply("Invalid timezone");
                    return;
                }
                try {
                    DateFormat df = new SimpleDateFormat("dd/MM HH:mm");
                    String[] validIDs = TimeZone.getAvailableIDs();
                    for (String str : validIDs) {
                        if (str != null && str.equals(args[0])) {
                            df.setTimeZone(TimeZone.getTimeZone(args[0]));
                            event.reply(args[0] + ": " + df.format(new Date()));
                            return;
                        }
                    }
                    event.reply("Invalid timezone");

                } catch (Exception e) {
                    event.reply("Something went wrong");
                    Utils.log(event.getMessage().getContentRaw(), Utils.ErrorTypes.ERROR);
                    Utils.log(e, Utils.ErrorTypes.ERROR);
                    e.printStackTrace();
                }
            }
        }
    }
}
