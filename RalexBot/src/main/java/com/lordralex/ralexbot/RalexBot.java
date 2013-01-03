package com.lordralex.ralexbot;

import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.settings.Settings;
import com.lordralex.ralexbot.threads.KeepAliveThread;
import com.lordralex.ralexbot.threads.KeyboardListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jline.console.ConsoleReader;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;

public final class RalexBot extends Thread {

    private static PircBotX driver;
    public static String VERSION = "0.0.4";
    private static EventHandler eventHandler;
    private static final RalexBot instance;
    private static KeyboardListener kblistener;
    private static Settings globalSettings;
    private static int exitCode = 0;
    private static KeepAliveThread kaThread;

    static {
        instance = new RalexBot();
    }

    public static void main(String[] args) {
        try {
            instance.createInstance();
        } catch (IOException | IrcException ex) {
            Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        synchronized (instance) {
            try {
                instance.wait();
                exitCode = 0;
            } catch (InterruptedException ex) {
                Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
                exitCode = 1;
            }
        }

        switch (exitCode) {
            case 0:
                System.out.println("Exiting bot");
            case 1:
                System.out.println("Bot returned exit code 1, restarting");
        }
        eventHandler.stopRunner();
        kblistener.interrupt();

        if (exitCode == 0) {
            System.exit(0);
        } else if (exitCode == 1) {
            main(args);
        }
    }

    private RalexBot() {
    }

    private void createInstance() throws IOException, IrcException {
        kblistener = new KeyboardListener(instance);
        globalSettings = Settings.loadGlobalSettings();

        driver = new PircBotX();
        driver.setVersion(VERSION);
        driver.setVerbose(false);
        String nick = globalSettings.getString("nick");
        if (nick == null || nick.isEmpty()) {
            nick = "DebugBot";
        }
        driver.setName(nick);
        driver.setLogin(nick);

        System.out.println("Nick of bot: " + driver.getNick());

        Utils.setUtils(driver);

        eventHandler = new EventHandler();
        boolean sucess = driver.getListenerManager().addListener(eventHandler);
        if (sucess) {
            System.out.println("Listener hook attached to bot");
        } else {
            System.out.println("Listener hook was unable to attach to the bot");
        }

        String network = globalSettings.getString("network");
        int port = globalSettings.getInt("port");

        if (network == null || network.isEmpty()) {
            network = "irc.esper.net";
        }
        if (port == 0 || port < 0) {
            port = 6667;
        }
        try {
            driver.connect(network, port);
        } catch (NickAlreadyInUseException ex) {
            Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
            driver.changeNick(globalSettings.getString("nick") + "_");
            driver.connect(network, port);
            driver.sendMessage("chanserv", "ghost " + globalSettings.getString("nick") + " " + globalSettings.getString("nick-pw"));
            driver.changeNick(globalSettings.getString("nick"));
            if (!globalSettings.getString("nick").equalsIgnoreCase(driver.getNick())) {
                System.err.println("Could not claim the nick " + globalSettings.getString("nick"));
            }
        }

        String id = globalSettings.getString("nick-pw");
        if (id != null && !id.isEmpty()) {
            driver.sendMessage("nickserv", "identify " + id);
            System.out.println("Logging in to nickserv with " + id);
        }

        List<String> channels = globalSettings.getStringList("channels");
        if (channels != null && !channels.isEmpty()) {
            for (String chan : channels) {
                driver.joinChannel(chan);
            }
        } else {
            driver.joinChannel("#ae97");
        }

        System.out.println("Initial loading complete, engaging listeners");
        eventHandler.startQueue();


        System.out.println("Starting keyboard listener");
        kblistener.start();

        int pingTime = globalSettings.getInt("ping-time");
        if (pingTime == 0) {
            pingTime = 120;
        }
        System.out.println("Starting keep alive thread, pinging every " + pingTime + " seconds");
        kaThread = new KeepAliveThread(this, driver, pingTime);
        kaThread.start();
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public ConsoleReader getConsole() {
        return kblistener.getJLine();
    }
}
