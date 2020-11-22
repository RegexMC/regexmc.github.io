package me.regexmc.jdaregexbot.commands.examples;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.util.Utils;

public class BaseCommand extends Command {

    public BaseCommand() {
        this.name = "base";
        this.help = "desc";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {

        }
    }
}
