package com.lordralex.ralexbot;

import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.settings.Settings;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.PrivateMessageEvent;

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
        kblistener = new KeyboardListener();
        kblistener.start();

        int pingTime = globalSettings.getInt("ping-time");
        if (pingTime == 0) {
            pingTime = 120;
        }
        System.out.println("Starting keep alive thread, pinging every " + pingTime + " seconds");
        kaThread = new KeepAliveThread(pingTime);
        kaThread.start();
    }

    private final static class KeyboardListener extends Thread {

        Scanner kb;

        public KeyboardListener() {
            setName("Keyboard_Listener_Thread");
            kb = new Scanner(System.in);
        }

        @Override
        public void run() {
            String line;
            boolean run = true;
            while (run) {
                line = kb.nextLine();
                if (line == null || line.trim().equalsIgnoreCase("stop")) {
                    run = false;
                }
            }
            synchronized (instance) {
                instance.notify();
            }
            System.out.println("Ending keyboard listener");
        }
    }

    private static final class KeepAliveThread extends Thread {

        int time = 120;

        public KeepAliveThread(int value) {
            time = value;
        }

        @Override
        public void run() {
            boolean stop = false;
            while (driver.isConnected() && !stop) {
                synchronized (this) {
                    try {
                        wait(time * 1000);
                    } catch (InterruptedException ex) {
                        stop = true;
                    }
                }
                driver.sendRawLine("PING");
                try {
                    PingThread pinger = new PingThread(this);
                    boolean wasEvent = false;
                    while (wasEvent) {
                        Event evt = driver.waitFor(Event.class);
                        if (evt instanceof PrivateMessageEvent) {
                        } else {
                            eventHandler.fireEvent(evt);
                        }
                    }
                    pinger.interrupt();
                } catch (InterruptedException ex) {
                    Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!driver.isConnected()) {
                    instance.interrupt();
                }
            }
        }
    }

    private static class PingThread extends Thread {

        Thread parentThread;

        public PingThread(Thread parent) {
            parentThread = parent;
        }

        @Override
        public void run() {
            synchronized (this) {
                try {
                    wait(30 * 1000);
                    System.out.println("Time expired, killing parent " + parentThread.getName());
                    parentThread.interrupt();
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}



