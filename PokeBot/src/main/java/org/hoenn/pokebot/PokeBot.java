/*
 * Copyright (C) 2013 Lord_Ralex
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hoenn.pokebot;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import jline.console.ConsoleReader;
import org.hoenn.pokebot.api.events.ConnectionEvent;
import org.hoenn.pokebot.eventhandler.EventHandler;
import org.hoenn.pokebot.extension.ExtensionManager;
import org.hoenn.pokebot.input.KeyboardListener;
import org.hoenn.pokebot.permissions.PermissionManager;
import org.hoenn.pokebot.scheduler.Scheduler;
import org.hoenn.pokebot.settings.Settings;
import org.hoenn.pokebot.stream.SplitPrintStream;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;

public final class PokeBot extends Thread {

    private static PircBotX driver;
    private static PokeBot instance;
    private static boolean debugMode = false;
    private static final Map<String, String> args = new HashMap<>();
    private static boolean login = true;
    public static final String VERSION = "6.0.0";

    public static void main(String[] startargs) throws IOException {
        SplitPrintStream out = new SplitPrintStream(System.out);
        SplitPrintStream err = new SplitPrintStream(System.err);
        System.setOut(out);
        System.setErr(err);
        if (startargs.length != 0) {
            for (String arg : startargs) {
                if (arg.equalsIgnoreCase("-debugmode")) {
                    log(Level.INFO, "Starting with DEBUG MODE ENABLED");
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
            instance = new PokeBot();
            instance.createInstance(args.get("user"), args.get("pass"));
        } catch (IOException | IrcException ex) {
            log(Level.SEVERE, "An error occurred", ex);
        }
        synchronized (instance) {
            try {
                instance.wait();
            } catch (InterruptedException ex) {
                log(Level.SEVERE, "The instance was interrupted", ex);
            }
        }

        instance.getEventHandler().stopRunner();
        try {
            driver.shutdown(false);
        } catch (Exception ex) {
            log(Level.SEVERE, "An error occured on shutting down", ex);
        }
        System.exit(0);
    }

    public static PokeBot getInstance() {
        return instance;
    }

    public static boolean getDebugMode() {
        return debugMode;
    }

    public static Map<String, String> getStartupArgs() {
        return args;
    }

    public static void log(String message) {
        log(Level.INFO, message);
    }

    public static void log(Level level, String message) {
        System.out.println("[" + level + "] " + message);
    }

    public static void log(Level level, String message, Throwable error) {
        log(level, message);
        error.printStackTrace(System.out);
    }
    private final EventHandler eventHandler;
    private final KeyboardListener kblistener;
    private final Settings globalSettings;
    private final PermissionManager permManager;
    private final ExtensionManager extensionManager;
    private final Scheduler scheduler;

    private PokeBot() {
        if (!(new File("config.yml").exists())) {
            try (InputStream input = PokeBot.class.getResourceAsStream("/config.yml")) {
                try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File("config.yml")))) {
                    try {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = input.read(buffer)) >= 0) {
                            output.write(buffer, 0, len);
                        }
                        input.close();
                        output.close();
                    } catch (IOException ex) {
                        log(Level.SEVERE, "An error occurred on copying the streams", ex);
                    }
                }
            } catch (IOException ex) {
                log(Level.SEVERE, "Error on saving config", ex);
            }
        }
        globalSettings = new Settings();
        try {
            globalSettings.load(new File("config.yml"));
        } catch (IOException e) {
            PokeBot.log(Level.SEVERE, "Could not load config.yml", e);
        }
        driver = new PircBotX();
        KeyboardListener temp;
        try {
            temp = new KeyboardListener(instance, driver);
        } catch (IOException ex) {
            temp = null;
            log(Level.SEVERE, "An error occured", ex);
        }
        kblistener = temp;
        eventHandler = new EventHandler(driver);
        extensionManager = new ExtensionManager();
        permManager = new PermissionManager();
        scheduler = new Scheduler();
    }

    private void createInstance(String user, String pass) throws IOException, IrcException {
        driver.setEncoding(Charset.forName("UTF-8"));
        String bind = globalSettings.getString("bind-ip");
        if (bind != null && !bind.isEmpty()) {
            InetAddress addr = InetAddress.getByName(bind);
            driver.setInetAddress(addr);
        }
        driver.setVerbose(true);
        driver.setVersion("PokeBot   - v" + VERSION);
        driver.setAutoReconnect(false);
        driver.setAutoReconnectChannels(true);
        String nick = user;
        if (nick == null || nick.isEmpty()) {
            nick = globalSettings.getString("nick");
        }
        if (nick == null || nick.isEmpty()) {
            nick = "DebugBot";
        }
        driver.setName(nick);
        driver.setLogin(nick);

        log(Level.INFO, "Nick of bot: " + nick);

        eventHandler.load();
        extensionManager.load();
        try {
            permManager.load();
        } catch (IOException e) {
            log(Level.SEVERE, "Error loading permissions file", e);
        }
        boolean eventSuccess = driver.getListenerManager().addListener(eventHandler);
        if (eventSuccess) {
            log(Level.INFO, "Listener hook attached to bot");
        } else {
            log(Level.INFO, "Listener hook was unable to attach to the bot");
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
        log(Level.INFO, "Connecting to: " + network + ":" + port);
        try {
            driver.connect(network, port);
        } catch (NickAlreadyInUseException ex) {
            log(Level.SEVERE, "The nick is already taken");
            driver.changeNick(nick + "_");
            driver.connect(network, port);
            driver.sendMessage("chanserv", "ghost " + nick + " " + pass);
            driver.changeNick(nick);
            if (!globalSettings.getString("nick").equalsIgnoreCase(driver.getNick())) {
                log(Level.SEVERE, "Could not claim the nick " + nick);
            }
        }
        if (pass != null && !pass.isEmpty() && login) {
            driver.sendMessage("nickserv", "identify " + pass);
            log(Level.INFO, "Logging in to nickserv");
        }
        eventHandler.fireEvent(new ConnectionEvent());
        List<String> channels = globalSettings.getStringList("channels");
        if (channels != null && !channels.isEmpty()) {
            for (String chan : channels) {
                log(Level.INFO, "Joining " + chan);
                driver.joinChannel(chan);
            }
        }
        log(Level.INFO, "Initial loading complete, engaging listeners");
        eventHandler.startQueue();
        log(Level.INFO, "Starting keyboard listener");
        kblistener.start();
        log(Level.INFO, "All systems operational");
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public ExtensionManager getExtensionManager() {
        return extensionManager;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public ConsoleReader getConsole() {
        return kblistener.getJLine();
    }

    public PermissionManager getPermManager() {
        return permManager;
    }

    public Settings getSettings() {
        return globalSettings;
    }
}
