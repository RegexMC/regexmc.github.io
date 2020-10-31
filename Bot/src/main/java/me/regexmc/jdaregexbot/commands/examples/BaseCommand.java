package me.regexmc.jdaregexbot.commands.examples;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.util.Utils;

import java.util.Arrays;

public class BaseCommand extends Command {

    public BaseCommand() {
        this.name = "base";
        this.help = "desc";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            String[] args = Arrays.copyOfRange(event.getMessage().getContentRaw().split(" "), 1, event.getMessage().getContentRaw().split(" ").length);
            System.out.println("test");
        }
    }
}
