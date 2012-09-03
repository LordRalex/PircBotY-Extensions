package com.lordralex.miniabsol.MiniAbsol;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;

public class MiniAbsol {

    private static PircBotX mainBot;
    private static EventManager manager;
    private static Settings fileManager;
    private static MiniAbsol instance;
    public static String VERSION = "0.0.1";

    public static void main(String[] args) {
        instance = new MiniAbsol();
    }

    private MiniAbsol() {
        mainBot = new PircBotX();
        mainBot.setVersion(VERSION);
        mainBot.setAutoNickChange(true);
        mainBot.setAutoSplitMessage(true);
        mainBot.setName("MiniAbsol");
        mainBot.setVerbose(false);
    }

    public void changeNick(String newNick) {
        mainBot.changeNick(newNick);
    }

    public void sendMessage(String target, String message) {
        if (target == null) {
            return;
        }
        mainBot.sendMessage(target, message);
    }

    public void sendMessage(String target, String[] messages) {
        if (target == null) {
            return;
        }
        for (String message : messages) {
            mainBot.sendMessage(target, message);
        }
    }

    public void joinChannel(String newChannel) {
        mainBot.joinChannel(newChannel);
    }

    public void leaveChannel(String channel) {
        Channel chan = mainBot.getChannel(channel);
        if (chan == null) {
            return;
        }
        mainBot.partChannel(chan);
    }

    public void sendNotice(String target, String message) {
        if (target == null) {
            return;
        }
        mainBot.sendNotice(target, message);
    }

    public void sendNotice(String target, String[] messages) {
        if (target == null) {
            return;
        }
        for (String message : messages) {
            mainBot.sendNotice(target, message);
        }
    }
}
