package com.lordralex.ralexbot;

import com.lordralex.ralexbot.api.Utilities;
import com.lordralex.ralexbot.api.users.BotUser;
import com.lordralex.ralexbot.console.ConsoleHandler;
import com.lordralex.ralexbot.console.ConsoleLogFormatter;
import com.lordralex.ralexbot.settings.Settings;
import com.lordralex.ralexbot.stream.LoggerOutputStream;
import com.lordralex.ralexbot.threads.KeyboardListener;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
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
    private static final Logger logger = Logger.getLogger(RalexBot.class.getName());
    private static boolean login = true;

    static {
        instance = new RalexBot();
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void main(String[] startargs) throws IOException {
        ConsoleLogFormatter clf = new ConsoleLogFormatter();
        ConsoleReader cr = new ConsoleReader(System.in, System.out);
        ConsoleHandler ch = new ConsoleHandler(cr);
        ch.setFormatter(clf);
        Logger temp = Logger.getLogger("");
        for (Handler handle : temp.getHandlers()) {
            temp.removeHandler(handle);
        }
        logger.addHandler(ch);
        if (startargs.length != 0) {
            for (String arg : startargs) {
                if (arg.equalsIgnoreCase("-debugmode")) {
                    logger.info("Starting with DEBUG MODE ENABLED");
                    debugMode = true;
                } else if (arg.equalsIgnoreCase("-nologin")) {
                    login = false;
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
            instance.createInstance(args.get("user"), args.get("pass"));
        } catch (IOException | IrcException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        synchronized (instance) {
            try {
                instance.wait();
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        driver.shutdown();
        System.exit(0);
    }

    private void createInstance(String user, String pass) throws IOException, IrcException {
        kblistener = new KeyboardListener(instance);
        globalSettings = Settings.loadGlobalSettings();

        driver = new PircBotX();
        driver.setVersion(VERSION);
        driver.setVerbose(false);
        String nick = user;
        if (nick == null || nick.isEmpty()) {
            nick = globalSettings.getString("nick");
        }
        if (nick == null || nick.isEmpty()) {
            nick = "DebugBot";
        }
        driver.setName(nick);
        driver.setLogin(nick);

        logger.info("Nick of bot: " + driver.getNick());

        Utilities.setUtils(driver);

        eventHandler = new EventHandler();
        eventHandler.load();
        boolean success = driver.getListenerManager().addListener(eventHandler);
        if (success) {
            logger.info("Listener hook attached to bot");
        } else {
            logger.info("Listener hook was unable to attach to the bot");
        }

        String network = globalSettings.getString("network");
        int port = globalSettings.getInt("port");

        if (network == null || network.isEmpty()) {
            network = "irc.esper.net";
        }
        if (port == 0 || port < 0) {
            port = 6667;
        }
        if (pass == null || pass.isEmpty()) {
            pass = globalSettings.getString("nick-pw");
        }
        try {
            driver.connect(network, port);
        } catch (NickAlreadyInUseException ex) {
            logger.log(Level.SEVERE, null, ex);
            driver.changeNick(nick + "_");
            driver.connect(network, port);
            driver.sendMessage("chanserv", "ghost " + nick + " " + pass);
            driver.changeNick(nick);
            if (!globalSettings.getString("nick").equalsIgnoreCase(driver.getNick())) {
                logger.severe("Could not claim the nick " + nick);
            }
        }

        BotUser bot = new BotUser();
        if (pass != null && !pass.isEmpty() && login) {
            bot.sendMessage("nickserv", "identify " + pass);
            logger.info("Logging in to nickserv");
        }

        List<String> channels = globalSettings.getStringList("channels");
        if (channels != null && !channels.isEmpty()) {
            for (String chan : channels) {
                bot.joinChannel(chan);
            }
        } else {
            bot.joinChannel("#ae97");
        }

        logger.info("Initial loading complete, engaging listeners");
        eventHandler.startQueue();


        logger.info("Starting keyboard listener");
        kblistener.start();

        logger.info("All systems operational");
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
