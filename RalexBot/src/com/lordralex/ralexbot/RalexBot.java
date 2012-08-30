package com.lordralex.ralexbot;

import com.lordralex.ralexbot.api.apihandlers.EventManager;
import com.lordralex.ralexbot.api.events.*;
import com.lordralex.ralexbot.file.FileSystem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class RalexBot {

    private static List<String> autoJoinChannels = new ArrayList<>();
    /**
     * The current version of the {@link RalexBot}
     */
    public static final String RBVERSION = "0.3.11";
    public EventManager manager;
    PircBotX bot;

    /**
     * This creates the bot. In this, it will load the settings, create the
     * commands, connect to the network, and join channels.
     *
     * @param debugState true will run DebugBot
     * @throws IrcException
     * @throws IOException
     */
    public RalexBot(boolean debugState) throws IrcException, IOException {
        long begin = System.currentTimeMillis();
        bot = new PircBotX();
        bot.setAutoNickChange(true);
        bot.setVersion(RBVERSION);
        System.out.println("Loading files: " + (System.currentTimeMillis() - begin) + " ms");
        FileSystem.loadFiles();
        if (!debugState) {
            String login = FileSystem.getString("nick");
            String password = FileSystem.getString("password");
            if (login.equalsIgnoreCase("")) {
                login = "RalexBot";
            }
            System.out.println("Logging In: " + (System.currentTimeMillis() - begin) + " ms");
            bot.setLogin(login);
            bot.setName(login);
            if (!password.equalsIgnoreCase("")) {
                bot.identify(password);
                bot.sendMessage("nickserv", "identify " + password);
            }
            System.out.println("Beginning setup: " + (System.currentTimeMillis() - begin) + " ms");
            setup(false);

        } else {
            System.out.println("Logging in: " + (System.currentTimeMillis() - begin) + " ms");
            bot.setLogin("DebugBot");
            bot.setName("DebugBot");
            System.out.println("Beginning setup: " + (System.currentTimeMillis() - begin) + " ms");
            setup(true);
        }
        System.out.println("Start time: " + (System.currentTimeMillis() - begin) + " ms");
    }

    private void setup(boolean debug) {

        bot.setVerbose(false);
        try {
            bot.connect("irc.esper.net");
        } catch (IOException | IrcException ex) {
            Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
            bot.disconnect();
            bot.quitServer();
            return;
        }

        File saveFolder = new File("settings");
        if (!saveFolder.exists()) {
            saveFolder.mkdirs();
        }
        File saveFolder1 = new File("data");
        if (!saveFolder1.exists()) {
            saveFolder1.mkdirs();
        }

        createListeners();

        autoJoinChannels = FileSystem.getStringList("auto-join");

        if (autoJoinChannels == null) {
            autoJoinChannels = new ArrayList<>();
        }

        if (!debug) {
            for (String channel : autoJoinChannels) {
                bot.joinChannel(channel);
            }
        } else {
            bot.joinChannel("#ae97");
        }
    }

    private void createListeners() {
        manager = new EventManager();
    }

    /**
     * This returns the online status of the bot. If false, the bot is confirmed
     * to not be online.
     *
     * @return True if the bot is online
     */
    public boolean isOnline() {
        return bot.isConnected();
    }

    public boolean isStopped() {
        return (!isOnline());
    }

    /**
     * This force stops the bot. ONLY USE IF IT IS NOT RESPONDING OTHERWISE!
     */
    public void forceStop() {
        bot.quitServer("Shutting down");
        bot.disconnect();
    }

    public PircBotX getBot() {
        return bot;
    }
}
