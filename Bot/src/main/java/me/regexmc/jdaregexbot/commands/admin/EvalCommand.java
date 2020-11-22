package me.regexmc.jdaregexbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.util.Utils;
import org.apache.commons.text.StringEscapeUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class EvalCommand extends Command {
    private String script = "";
    private String mentions = "";
    private String mentionsIds = "";
    private String fileContent = "";

    public EvalCommand() {
        this.name = "eval";
        this.cooldown = 60;
        this.ownerCommand = true;
        this.arguments = "<js>";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nRuns javascript code";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
        this.category = Utils.CommandCategories.GENERIC.getCategory();
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            try {
                script = "";
                mentions = "";
                mentionsIds = "";
                fileContent = "";

                String input = event.getMessage().getContentRaw().split(" ", 2)[1];


                if (input.startsWith("```")) {
                    input = input.substring(3);
                    input = input.substring(0, input.length() - 3);
                    input = input.replaceAll("\\n", " ");
                }

                Context cx = Context.enter();
                Scriptable scope = cx.initStandardObjects();

                event.getMessage().getMentionedUsers().forEach(m -> {
                    if (mentionsIds.length() == 0) {
                        mentionsIds += String.format("[\"%s\"]", m.getId());
                        mentions += String.format("[\"%s\"]", m.getName());
                    } else {
                        mentionsIds = mentionsIds.substring(0, mentionsIds.length() - 1);
                        mentionsIds += String.format(",\"%s\"]", m.getId());
                        mentions = mentions.substring(0, mentions.length() - 1);
                        mentions += String.format(",\"%s\"]", m.getName());
                    }
                });

                if (event.getMessage().getAttachments().size() > 0) {
                    event.getMessage().getAttachments().forEach(a -> {
                        try {
                            File attachment = Utils.downloadAttachmentToFile(event.getMessage().getAttachments(), event.getMessage());

                            String content = Files.readString(Path.of(attachment.getAbsolutePath()));
                            if (fileContent.length() == 0) {
                                fileContent += String.format("[\"%s\"]", content);
                            } else {
                                fileContent = fileContent.substring(0, fileContent.length() - 1);
                                fileContent += String.format(",\"%s\"]", content);
                            }
                        } catch (IOException e) {
                            event.reply("Error downloading attachment.");
                        }
                    });
                }

                appendScript(String.format("var message = \"%s\"", StringEscapeUtils.escapeJava(event.getMessage().getContentRaw())));
                appendScript(String.format("var messageId =\"%s\"", event.getMessage().getId()));
                appendScript(String.format("var author = \"%s\"", event.getMessage().getAuthor().getName()));
                appendScript(String.format("var authorId = \"%s\"", event.getMessage().getAuthor().getId()));
                appendScript(String.format("var channel = \"%s\"", event.getMessage().getChannel().getName()));
                appendScript(String.format("var channelId = \"%s\"", event.getMessage().getChannel().getId()));
                appendScript(String.format("var guild = \"%s\"", event.getMessage().getGuild().getName()));
                appendScript(String.format("var guildId = \"%s\"", event.getMessage().getGuild().getId()));
                appendScript("function getRates(x) { return (60/x)*60*24 }");
                appendScript("function toEnchanted(x) { return x/(32*5) }");
                appendScript("var eRedstonePrice = 160");
                appendScript("var eCobblestonePrice = 160");
                appendScript("var eEndstonePrice = 320");
                appendScript("var eCoalPrice = 320");
                appendScript("var clayPrice = 3");
                appendScript("var eClayPrice = 480");
                appendScript("var eGoldPrice = 640");
                appendScript("var eDiamondPrice = 1280");


                if (mentions.length() > 0) {
                    appendScript(String.format("var mentions = %s", mentions));
                    appendScript(String.format("var mentionsIds = %s", mentionsIds));
                }
                if (fileContent.length() > 0)
                    appendScript(String.format("var attachmentContents = %s", fileContent));
                appendScript("function random(min, max) { return Math.floor((Math.random() * max) + min); }");
                appendScript("function vars() { return \"message(Id), author(Id), channel(Id), guild(Id), mentions(Ids), attachementContents[0]\" }");
                appendScript(input);

                Utils.log(script, Utils.ErrorTypes.INFO);

                event.reply(cx.evaluateString(scope, script, "EvaluationScript", 1, null).toString());
                Context.exit();
            } catch (Exception e) {
                event.reply("```" + e.getMessage() + "```");
            }
        } else {
            event.reply("You cant use this command nerd");
        }

    }

    private void appendScript(String s) {
        script += s + ";\n";
    }
}