package me.regexmc.jdaregexbot.util;

import java.awt.*;

public class RankUtils {
    public enum Ranks {
        ADMIN(Color.decode("#FF5555"), "ADMIN", "ADMIN"),
        MODERATOR(Color.decode("#00AA00"), "MODERATOR", "MODERATOR"),
        HELPER(Color.decode("#0000AA"), "HELPER", "HELPER"),
        YOUTUBE(Color.decode("#FF5555"), "YOUTUBE", "YOUTUBE"),
        MVP_PLUSPLUS(Color.decode("#FFAA00"), "MVP++", "MVP_PLUSPLUS"),
        MVP_PLUS(Color.decode("#55FFFF"), "MVP+", "MVP_PLUS"),
        MVP(Color.decode("#55FFFF"), "MVP", "MVP"),
        VIP_PLUS(Color.decode("#55FF55"), "VIP+", "VIP_PLUS"),
        VIP(Color.decode("#55FF55"), "VIP", "VIP"),
        NONE(Color.decode("#AAAAAA"), "NONE", "NONE");

        private final Color color;
        private final String chat;
        private final String string;

        Ranks(Color color, String chat, String string) {
            this.color = color;
            this.chat = chat;
            this.string = string;
        }

        public static Ranks getRankFromString(String in) {
            for (Ranks rank : Ranks.values()) {
                if (rank.string.equals(in)) return rank;
            }
            return Ranks.NONE;
        }

        public Color getColor() {
            return color;
        }

        public String getChat() {
            return chat;
        }

    }
}
