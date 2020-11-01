package me.regexmc.jdaregexbot.commands.admin.imagemanipulation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.regexmc.jdaregexbot.util.Utils;

import java.util.Arrays;

public class ResizeImageCommand extends Command {

    public ResizeImageCommand(EventWaiter waiter) {
        this.name = "base";
        this.help = "desc";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            String[] args = event.getArgs().split(" ");

            if (args.length > 0) {
                if (args.length > 1) {
                    //
                } else {
                    //scale
                }
            }
        }
    }
}
