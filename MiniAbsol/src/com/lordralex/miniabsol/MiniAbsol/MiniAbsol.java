package com.lordralex.miniabsol.MiniAbsol;

import com.lordralex.miniabsol.MiniAbsol.managers.EventManager;
import com.lordralex.miniabsol.MiniAbsol.managers.Settings;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;

public class MiniAbsol {

    private static PircBotX mainBot;
    private static EventManager manager;
    private static Settings fileManager;
    private static MiniAbsol instance;
    public static String VERSION = "0.0.1";

    public static void main(String[] args) throws IOException, IrcException {
        instance = new MiniAbsol();
        fileManager = new Settings(instance);
        manager = new EventManager(instance);

        mainBot.connect("irc.esper.net");

        mainBot.joinChannel("#ae97");
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

    public User getUser(String name) {
        return mainBot.getUser(name);
    }

    public Channel getChannel(String name) {
        return mainBot.getChannel(name);
    }

    public EventManager getManager() {
        return manager;
    }

    public Settings getSettings() {
        return fileManager;
    }
}
