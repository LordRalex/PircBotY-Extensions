package com.lordralex.ralexbot;

import com.lordralex.ralexbot.api.Utilities;
import com.lordralex.ralexbot.api.users.BotUser;
import com.lordralex.ralexbot.settings.Settings;
import com.lordralex.ralexbot.threads.KeyboardListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jline.console.ConsoleReader;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;

public final class RalexBot extends Thread {

    private static PircBotX driver;
    public static String VERSION = "BOT-VERSION";
    private static EventHandler eventHandler;
    private static final RalexBot instance;
    private static KeyboardListener kblistener;
    private static Settings globalSettings;
    private static boolean debugMode = false;
    private static final Map<String, String> args = new HashMap<>();

    static {
        instance = new RalexBot();
    }

    public static void main(String[] startargs) {
        if (startargs.length != 0) {
            for (String arg : startargs) {
                if (arg.equalsIgnoreCase("-debugmode")) {
                    System.out.println("Starting with DEBUG MODE ENABLED");
                    debugMode = true;
                } else {
                    String[] argument = arg.split("=");
                    String key, value;
                    key = argument[0];
                    if (argument.length == 1) {
                        value = "true";
                    } else {
                        value = argument[1];
                    }
                    args.put(key, value);
                }
            }
        }
        try {
            instance.createInstance();
        } catch (IOException | IrcException ex) {
            Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        synchronized (instance) {
            try {
                instance.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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

        Utilities.setUtils(driver);

        eventHandler = new EventHandler();
        eventHandler.load();
        boolean success = driver.getListenerManager().addListener(eventHandler);
        if (success) {
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

        BotUser bot = new BotUser();

        String id = globalSettings.getString("nick-pw");
        if (id != null && !id.isEmpty()) {
            bot.sendMessage("nickserv", "identify " + id);
            System.out.println("Logging in to nickserv");
        }

        List<String> channels = globalSettings.getStringList("channels");
        if (channels != null && !channels.isEmpty()) {
            for (String chan : channels) {
                bot.joinChannel(chan);
            }
        } else {
            bot.joinChannel("#ae97");
        }

        System.out.println("Initial loading complete, engaging listeners");
        eventHandler.startQueue();


        System.out.println("Starting keyboard listener");
        kblistener.start();

        System.out.println("All systems operational");
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public ConsoleReader getConsole() {
        return kblistener.getJLine();
    }

    public static boolean getDebugMode() {
        return debugMode;
    }

    public static Map<String, String> getStartupArgs() {
        return args;
    }

    private RalexBot() {
    }
}
