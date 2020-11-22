package me.regexmc.jdaregexbot.commands.hypixel;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.regexmc.jdaregexbot.BotMain;
import me.regexmc.jdaregexbot.util.Utils;
import org.json.JSONObject;

import java.io.IOException;

public class SetTrackingChannel extends Command {
    private final EventWaiter waiter;

    public SetTrackingChannel(EventWaiter waiter) {
        this.name = "settrackingchannel";
        this.cooldown = 60;
        this.arguments = "<uuid>";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nSet channel for tracking that UUID.";
        this.waiter = waiter;
        this.category = Utils.CommandCategories.HYPIXEL.getCategory();
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String[] args = event.getArgs().split(" ");

            if (args.length < 1) {
                event.reply("Not enough arguments supplied (Refer to help)");
            } else {
                String uuid = args[0];

                JSONObject keysJSON = Utils.readJsonFromFile(BotMain.config.get("path_json") + "apikeys.json");
                if (!keysJSON.has(uuid)) {
                    event.reply("Tracking not set for player (use !!setkey in DMs)");
                    return;
                }

                JSONObject keyInfo = keysJSON.getJSONObject(uuid);
                keyInfo.remove("channel");
                keyInfo.put("channel", event.getChannel().getId());

                keysJSON.remove(uuid);
                keysJSON.put(uuid, keyInfo);

                Utils.writeToFile(BotMain.config.get("path_json") + "apikeys.json", keysJSON.toString());

                event.reply("Set tracking for player to this channel");
            }
        } catch (IOException e) {
            event.reply("Something went wrong");
            Utils.log(e, Utils.ErrorTypes.ERROR);
        }
    }
}
