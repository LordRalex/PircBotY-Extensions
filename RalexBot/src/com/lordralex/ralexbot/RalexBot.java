package com.lordralex.ralexbot;

import com.lordralex.ralexbot.settings.Settings;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

public final class RalexBot {

    private static PircBotX driver;
    public static String VERSION = "0.0.1";
    private static EventHandler eventHandler;

    public static void main(String[] args) throws IOException, IrcException {

        Settings.loadSettings();

        driver = new PircBotX();
        driver.setVersion(VERSION);
        driver.setVerbose(false);
        driver.setName(Settings.getString("nick"));
        System.out.println("Nick of bot: " + driver.getNick());

        eventHandler = new EventHandler(driver);
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
        driver.connect(network, port);

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
        }
        else
        {
            driver.joinChannel("#ae97");
        }

        System.out.println("Initial loading complete, engaging listeners");

        KeyboardListener listener = new KeyboardListener();
        listener.start();

        while (driver.isConnected() && listener.isAlive()) {
        }

        System.out.println("Bot terminating from server");
        if (driver.isConnected()) {
            driver.shutdown();
        }
        if (listener.isAlive()) {
            listener.interrupt();
        }
        System.out.println("Bot shut down");

        System.exit(0);
    }

    private static class KeyboardListener extends Thread {

        Scanner keyboard;

        public KeyboardListener() {
            keyboard = new Scanner(System.in);
        }

        @Override
        public void run() {
            String line = null;
            boolean run = true;
            while (run) {
                line = keyboard.nextLine();
                if (line == null || line.trim().equalsIgnoreCase("stop")) {
                    run = false;
                }
            }
        }
    }
}