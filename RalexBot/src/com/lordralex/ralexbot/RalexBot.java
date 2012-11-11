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

public final class RalexBot {

    private static PircBotX driver;
    public static String VERSION = "0.0.2";
    private static EventHandler eventHandler;
    private static final RalexBot instance;

    static {
        instance = new RalexBot();
    }

    public static void main(String[] args) {
        try {
            instance.createInstance();
            synchronized (instance) {
                try {
                    instance.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException | IrcException ex) {
            Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Exiting bot");
        eventHandler.stopRunner();
        System.exit(0);
    }

    private RalexBot() {
    }

    private void createInstance() throws IOException, IrcException {
        Settings.loadSettings();

        driver = new PircBotX();
        driver.setVersion(VERSION);
        driver.setVerbose(false);
        String nick = Settings.getString("nick");
        if (nick == null || nick.isEmpty()) {
            nick = "DebugBot";
        }
        driver.setName(nick);
        System.out.println("Nick of bot: " + driver.getNick());

        Utils.setUtils(driver);

        eventHandler = new EventHandler();
        boolean sucess = driver.getListenerManager().addListener(eventHandler);
        if (sucess) {
            System.out.println("Listener hook attached to bot");
        } else {
            System.out.println("Listener hook was unable to attach to the bot");
        }

        String network = Settings.getString("network");
        int port = Settings.getInt("port");

        if (network == null || network.isEmpty()) {
            network = "irc.esper.net";
        }
        if (port == 0) {
            port = 6667;
        }
        try {
            driver.connect(network, port);
        } catch (NickAlreadyInUseException ex) {
            Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
            driver.setName(driver.getNick() + "1");
            driver.connect(network, port);
            driver.sendMessage("chanserv", "ghost " + Settings.getString("nick") + " " + Settings.getString("nick-pw"));
            driver.changeNick(Settings.getString("nick"));
        }

        String id = Settings.getString("nick-pw");
        if (id != null && !id.isEmpty()) {
            driver.sendMessage("nickserv", "identify " + id);
            System.out.println("Logging in to nickserv with " + id);
        }

        List<String> channels = Settings.getStringList("channels");
        if (channels != null && !channels.isEmpty()) {
            for (String chan : channels) {
                driver.joinChannel(chan);
            }
        } else {
            driver.joinChannel("#ae97");
        }

        System.out.println("Initial loading complete, engaging listeners");
        eventHandler.startQueue();

        KeyboardListener listener = new KeyboardListener();
        listener.start();
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
}
