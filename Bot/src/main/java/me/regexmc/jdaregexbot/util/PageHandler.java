package me.regexmc.jdaregexbot.util;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.concurrent.TimeUnit;

public class PageHandler {
    public static void managePages(EventWaiter waiter, User allowedRespondent, Message message, MessageEmbed[] pages, int page, boolean addReactions) {
        if (addReactions) message.addReaction(Utils.Emotes.LEFT_ARROW.getUnicode()).queue();
        if (addReactions) message.addReaction(Utils.Emotes.RIGHT_ARROW.getUnicode()).queue();

        waiter.waitForEvent(MessageReactionAddEvent.class, reactMessage -> reactMessage.getMessageId().equals(message.getId()) && reactMessage.getUserId().equals(allowedRespondent.getId()), reactMessage -> {
            if (reactMessage.getReaction().getReactionEmote().toString().equals("RE:" + Utils.Emotes.RIGHT_ARROW.getUnicode())) {
                message.removeReaction(Utils.Emotes.RIGHT_ARROW.getUnicode(), allowedRespondent).queue();
                if (page + 1 == pages.length) {
                    managePages(waiter, allowedRespondent, message, pages, page, false);
                    return;
                }

                message.editMessage(pages[page + 1]).queue();
                managePages(waiter, allowedRespondent, message, pages, page + 1, false);
            } else if (reactMessage.getReaction().getReactionEmote().toString().equals("RE:" + Utils.Emotes.LEFT_ARROW.getUnicode())) {
                message.removeReaction(Utils.Emotes.LEFT_ARROW.getUnicode(), allowedRespondent).queue();
                if (page - 1 == -1) {
                    managePages(waiter, allowedRespondent, message, pages, page, false);
                    return;
                }

                message.editMessage(pages[page - 1]).queue();
                managePages(waiter, allowedRespondent, message, pages, page - 1, false);
            }
        }, 60, TimeUnit.SECONDS, () -> {
        });
    }
}
