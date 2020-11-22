package me.regexmc.jdaregexbot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.regexmc.jdaregexbot.commands.HelpCommand;
import me.regexmc.jdaregexbot.commands.admin.*;
import me.regexmc.jdaregexbot.commands.admin.anime.*;
import me.regexmc.jdaregexbot.commands.admin.imagemanipulation.CompressImageCommand;
import me.regexmc.jdaregexbot.commands.hypixel.*;
import me.regexmc.jdaregexbot.scheduled.AnimeNotification;
import me.regexmc.jdaregexbot.scheduled.StatsTracker;
import me.regexmc.jdaregexbot.util.APIUtil;
import me.regexmc.jdaregexbot.util.TrackerPlayerObject;
import me.regexmc.jdaregexbot.util.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.hypixel.api.HypixelAPI;
import org.yaml.snakeyaml.Yaml;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BotMain extends ListenerAdapter {
    public static JDABuilder builder;
    public static JDA bot;
    public static CommandClientBuilder client;
    public static CommandClient builtClient;
    public static Map<String, Object> config;
    public static ArrayList<String> admins = new ArrayList<>();
    public static ArrayList<String> commandChannels = new ArrayList<>();
    public static HashMap<String, TrackerPlayerObject> oldTrackerPlayerObjectHashMap = new HashMap<>();

    public static String logFile;
    public static EventWaiter waiter;

    private static String api_key;
    private static String token;

    public static void main(String[] args) throws IOException, LoginException, IllegalArgumentException {
        client = new CommandClientBuilder().useHelpBuilder(false);
        client.setActivity(Activity.watching("you"));
        client.setOwnerId("202666531111436288");
        client.setCoOwnerIds("426722323798818818");
        client.setPrefix("!!");

        waiter = new EventWaiter();
        loadConfig();

        Utils.log("Loaded config", Utils.ErrorTypes.INFO);

        Utils.log("Loading commands", Utils.ErrorTypes.INFO);

        client.addCommands(
                new HelpCommand(waiter),
                new StatsCommand(waiter),
                new SkywarsCommand(waiter),
                new RankedCommand(waiter),
                new BedwarsCommand(waiter),
                new SetKeyCommand(waiter),
                new SetTrackingChannel(waiter),
                new EvalCommand(),
                new CharCountCommand(),
                new ClearCacheCommand(waiter),
                new TimezoneCommand(),
                new ConvertCommand(),
                new CompressImageCommand(),
                new ParseDateCommand(),
                new PurgeSelfCommand(),
                new AiringCommand(waiter),
                new AnilistCommand(),
                new AnimeSearchCommand(),
                new HentaiSearchCommand(),
                new RandomHentaiCommand(),
                new WatchlistCommand(waiter));

        Utils.log("Loaded commands", Utils.ErrorTypes.INFO);
        Utils.log("Creating JDABuilder", Utils.ErrorTypes.INFO);
        builtClient = client.build();

        builder = JDABuilder.createDefault(token)
                .addEventListeners(waiter, builtClient)
                .addEventListeners(new BotMain());
        bot = builder.build();

        Utils.log("Set JDA", Utils.ErrorTypes.INFO);
        Utils.log("Setting Hypixel API", Utils.ErrorTypes.INFO);

        APIUtil.API = new HypixelAPI(UUID.fromString(api_key));

        Utils.log("Set Hypixel API", Utils.ErrorTypes.INFO);

        Utils.log("Starting Notification timer loop", Utils.ErrorTypes.INFO);
        Timer timer = new Timer();

        //Get the next hour quarter and start loop from then
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int mod = calendar.get(Calendar.MINUTE) % 15;
        calendar.add(Calendar.MINUTE, mod < 8 ? -mod : (15 - mod));
        long dateDiff = Utils.getDateDiff(now, calendar.getTime(), TimeUnit.MINUTES);
        long minutes = dateDiff < 0 ? 15 + dateDiff : dateDiff;

        timer.schedule(new AnimeNotification(), minutes * 60 * 1000, 30 * 60 * 1000);
        timer.schedule(new StatsTracker(), 15 * 1000, 15 * 1000);

        Utils.log("Started Notification timer loop", Utils.ErrorTypes.INFO);
    }

    @SuppressWarnings("unchecked")
    private static void loadConfig() throws IOException {
        Yaml yaml = new Yaml();
        String configYaml = Utils.loadResourceAsString("config.yml");
        config = yaml.load(configYaml);
        token = config.get("token").toString();
        api_key = config.get("api_key").toString();
        admins = (ArrayList<String>) config.get("admins");
        commandChannels = (ArrayList<String>) config.get("channel_commands");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannel().getId().equals("768670023400423434") || event.getChannel().getId().equals("772734834865995796") || event.getChannel().getId().equals("775545930962567178")) {
            if (event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId()))
                return; //dont delete if message is from self
            if(!event.getMessage().getContentRaw().startsWith("!!setkey")) { //will fuck up command
                event.getMessage().delete().queue();
            }
        } else if (event.getAuthor().getId().equals("202666531111436288")) {
            if (event.getMessage().getContentRaw().toLowerCase().equals("!!shutdown")) {
                event.getChannel().sendMessage("Shutting down").queue();
                try {
                    event.wait(1000);
                    event.getJDA().shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
