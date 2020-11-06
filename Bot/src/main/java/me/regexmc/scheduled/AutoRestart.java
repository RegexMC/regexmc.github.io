package me.regexmc.scheduled;

import me.regexmc.jdaregexbot.BotMain;

import java.util.TimerTask;

public class AutoRestart extends TimerTask {
    public void run() {
        BotMain.restartBot();
    }
}
