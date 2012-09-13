package com.lordralex.ralexbot;

import com.lordralex.ralexbot.settings.Settings;
import java.io.IOException;
import java.util.Scanner;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

public class RalexBot {

    private static PircBotX driver;
    public static String VERSION = "0.0.1";
    private static EventHandler eventHandler;

    public static void main(String[] args) throws IOException, IrcException {

        Settings.loadSettings();

        driver = new PircBotX();
        driver.setVersion(VERSION);
        driver.setName(Settings.getString(null));

        eventHandler = new EventHandler();
        boolean sucess = driver.getListenerManager().addListener(eventHandler);
        if (sucess) {
            System.out.println("Listener hook attached to bot");
        } else {
            System.out.println("Listener hook was unable to attach to the bot");
        }

        driver.connect("irc.esper.net", 6667);
        String id = Settings.getString("nick-pw");
        if (id != null && !id.isEmpty()) {
            driver.sendMessage("nickserv", "identify " + id);
            System.out.println("Logging in to nickserv with " + id);
        }
        KeyboardListener listener = new KeyboardListener();
        listener.start();

        while (driver.isConnected() || listener.isAlive()) {
        }
        System.out.println("Bot terminating from server");
        if (driver.isConnected()) {
            driver.shutdown();
        }
        if (listener.isAlive()) {
            listener.interrupt();
        }
        System.out.println("Bot shut down");
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
                if (line.trim().equalsIgnoreCase("stop")) {
                    run = false;
                }
            }
        }
    }
}