package me.regexmc.jdaregexbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.util.Utils;

import java.util.Arrays;

public class CharCountCommand extends Command {

    public CharCountCommand() {
        this.name = "charcount";
        this.cooldown = 5;
        this.ownerCommand = true;
        this.arguments = "<input>";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nGets character count of <input>";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            event.reply(Arrays.toString(event.getMessage().getContentRaw().split(" ", 2)).length() - 15 + "");

        }
    }
}

