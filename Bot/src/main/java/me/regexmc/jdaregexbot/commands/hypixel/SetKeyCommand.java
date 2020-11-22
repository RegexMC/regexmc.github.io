package me.regexmc.jdaregexbot.commands.hypixel;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.regexmc.jdaregexbot.BotMain;
import me.regexmc.jdaregexbot.util.Utils;
import org.json.JSONObject;

import java.io.IOException;

public class SetKeyCommand extends Command {
    private final EventWaiter waiter;

    public SetKeyCommand(EventWaiter waiter) {
        this.name = "setkey";
        this.cooldown = 60;
        this.arguments = "<uuid> <hypixel api key>";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nSet api key and UUID for stats tracker. DO NOT USE IN PUBLIC SERVERS. RESET API KEY IF YOU DO.";
        this.waiter = waiter;
        this.category = Utils.CommandCategories.HYPIXEL.getCategory();
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getMessage().isFromGuild()) {
            event.reply("<@" + event.getAuthor().getId() + "> PLEASE **RESET YOUR HYPIXEL API KEY IMMEDIATELY** IF YOU INCLUDED IT IN THE COMMAND, AND RUN COMMAND IN MY DIRECT MESSAGES");
            event.getMessage().delete().queue();
            return;
        }

        try {
            String[] args = event.getArgs().split(" ");

            if (args.length < 2) {
                event.reply("Not enough arguments supplied (Refer to help)");
            } else {
                String uuid = args[0];
                String key = args[1];

                try {
                    if (!((args.length > 2) && args[2].equals("-bypass") && Utils.isAdmin(event))) {
                        String url = "https://api.hypixel.net/key?key=" + key;
                        JSONObject json = Utils.readJsonFromUrl(url);
                        if (!json.getJSONObject("record").getString("owner").replace("-", "").equals(uuid.replace("-", ""))) {
                            event.reply("You are not owner of API Key. Please use your own API key.");
                            return;
                        }
                    }
                } catch (IOException e) {
                    event.reply("Invalid API key or UUID");
                    return;
                }

                JSONObject keysJSON = Utils.readJsonFromFile(BotMain.config.get("path_json") + "apikeys.json");
                if (keysJSON.has(uuid))
                    keysJSON.remove(uuid);

                JSONObject keyInfo = new JSONObject();
                keyInfo.put("id", event.getAuthor().getId());
                keyInfo.put("key", key);
                keyInfo.put("channel", "");

                keysJSON = keysJSON.put(uuid, keyInfo);

                Utils.writeToFile(BotMain.config.get("path_json") + "apikeys.json", keysJSON.toString());

                event.reply("API key set.");
            }
        } catch (IOException e) {
            event.reply("Something went wrong");
            Utils.log(e, Utils.ErrorTypes.ERROR);
        }
    }
}
