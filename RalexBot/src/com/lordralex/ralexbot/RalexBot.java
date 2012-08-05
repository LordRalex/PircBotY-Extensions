package com.lordralex.ralexbot;

import com.lordralex.ralexbot.api.apihandlers.EventManager;
import com.lordralex.ralexbot.api.events.*;
import com.lordralex.ralexbot.file.FileSystem;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class RalexBot extends PircBot {

    private static List<String> autoJoinChannels = new ArrayList<>();
    /**
     * The current version of the {@link RalexBot}
     */
    public static final String RBVERSION = "0.3.1";
    public EventManager manager;

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
        this.setAutoNickChange(true);
        this.setVersion(VERSION);
        System.out.println("Loading files: " + (System.currentTimeMillis() - begin) + " ms");
        FileSystem.loadFiles();
        if (!debugState) {
            String login = FileSystem.getString("nick");
            String password = FileSystem.getString("password");
            if (login.equalsIgnoreCase("")) {
                login = "RalexBot";
            }
            System.out.println("Logging In: " + (System.currentTimeMillis() - begin) + " ms");
            this.setLogin(login);
            this.setName(login);
            if (!password.equalsIgnoreCase("")) {
                this.identify(password);
                this.sendMessage("nickserv", "identify " + password);
            }
            System.out.println("Beginning setup: " + (System.currentTimeMillis() - begin) + " ms");
            setup(false);

        } else {
            System.out.println("Logging in: " + (System.currentTimeMillis() - begin) + " ms");
            this.setLogin("DebugBot");
            this.setName("DebugBot");
            System.out.println("Beginning setup: " + (System.currentTimeMillis() - begin) + " ms");
            setup(true);
        }
        System.out.println("Start time: " + (System.currentTimeMillis() - begin) + " ms");
    }

    private void setup(boolean debug) {

        this.setVerbose(false);
        try {
            this.connect("irc.esper.net");
        } catch (IOException | IrcException ex) {
            Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
            this.disconnect();
            this.quitServer();
            this.dispose();
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
                this.joinChannel(channel);
            }
        } else {
            this.joinChannel("#ae97");
        }
    }

    private void createListeners() {
        manager = new EventManager();
    }

    @Override
    public void onMessage(final String channel, final String sender, final String login, final String hostname, final String message) {
        if (message.startsWith("*")) {
            String[] parts = message.split(" ");
            String command = parts[0].substring(1, parts[0].length());
            String[] args = new String[parts.length - 1];
            for (int i = 0; i < args.length; i++) {
                args[i] = parts[i + 1];
            }

            CommandEvent event = new CommandEvent(command, sender, channel, args);
            manager.runEvent(event);
        } else {
            MessageEvent event = new MessageEvent(channel, sender, login, hostname, message);
            manager.runEvent(event);
        }
    }

    @Override
    public void onJoin(final String channel, final String sender, final String login, final String hostname) {
        Event event = new JoinEvent(channel, sender, login, hostname);
        manager.runEvent(event);
    }

    @Override
    public void onPrivateMessage(final String sender, final String login, final String hostname, final String message) {
        if (message.startsWith("*")) {
            String[] parts = message.split(" ");
            String command = parts[0].substring(1, parts[0].length());
            String[] args = new String[parts.length - 1];
            for (int i = 0; i < args.length; i++) {
                args[i] = parts[i + 1];
            }

            CommandEvent event = new CommandEvent(command, sender, null, args);
            manager.runEvent(event);
        } else {
            PrivateMessageEvent event = new PrivateMessageEvent(sender, login, hostname, message);
            manager.runEvent(event);
        }
    }

    @Override
    protected void onPart(final String channel, final String sender, final String login, final String hostname) {
        super.onPart(channel, sender, login, hostname);
        if (sender.equalsIgnoreCase(this.getNick())) {
            return;
        }
        Event event = new PartEvent(channel, sender, login, hostname);
        manager.runEvent(event);
    }

    @Override
    protected void onQuit(final String sourceNick, final String sourceLogin, final String sourceHostname, final String reason) {
        super.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
        if (sourceNick.equalsIgnoreCase(getNick())) {
            return;
        }
        Event event = new QuitEvent(sourceNick, sourceLogin, sourceHostname, reason);
        manager.runEvent(event);
    }

    @Override
    protected void onNickChange(final String oldNick, final String login, final String hostname, final String newNick) {
        super.onNickChange(oldNick, login, hostname, newNick);
        Event event = new NickChangeEvent(oldNick, login, hostname, newNick);
        manager.runEvent(event);
    }

    /**
     * This returns the online status of the bot. If false, the bot is confirmed
     * to not be online.
     *
     * @return True if the bot is online
     */
    public boolean isOnline() {
        return isConnected();
    }

    public boolean isStopped() {
        return (!isOnline());
    }

    /**
     * This force stops the bot. ONLY USE IF IT IS NOT RESPONDING OTHERWISE!
     */
    public void forceStop() {
        this.quitServer("Shutting down");
        this.disconnect();
    }
}
