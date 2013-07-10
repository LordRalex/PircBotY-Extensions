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
package net.ae97.ralexbot;

import net.ae97.ralexbot.api.Utilities;
import net.ae97.ralexbot.api.events.ConnectionEvent;
import net.ae97.ralexbot.api.exceptions.NickNotOnlineException;
import net.ae97.ralexbot.api.users.BotUser;
import net.ae97.ralexbot.permissions.PermissionManager;
import net.ae97.ralexbot.settings.Settings;
import net.ae97.ralexbot.threads.KeyboardListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jline.console.ConsoleReader;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;

public final class RalexBot extends Thread {

    private static final PircBotX driver;
    public static final String VERSION = "1.1.4";
    private static final EventHandler eventHandler;
    private static final RalexBot instance;
    private static final KeyboardListener kblistener;
    private static final Settings globalSettings;
    private static boolean debugMode = false;
    private static final Map<String, String> args = new HashMap<>();
    private static boolean login = true;
    private static final PermissionManager permManager;

    static {
        instance = new RalexBot();
        KeyboardListener temp;
        try {
            temp = new KeyboardListener(instance);
        } catch (IOException | NickNotOnlineException ex) {
            temp = null;
            logSevere("An error occured", ex);
        }
        kblistener = temp;
        if (!(new File("settings", "config.yml").exists())) {
            new File("settings").mkdirs();
            InputStream input = RalexBot.class.getResourceAsStream("/config.yml");
            try {
                BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File("settings", "config.yml")));
                RalexBot.copyInputStream(input, output);
            } catch (FileNotFoundException ex) {
                logSevere("Cannot find the config file", ex);
            }
        }
        globalSettings = Settings.loadGlobalSettings();
        driver = new PircBotX();
        eventHandler = new EventHandler(driver);
        permManager = new PermissionManager();
    }

    public static void main(String[] startargs) throws IOException {
        SplitStream out = new SplitStream(System.out);
        SplitStream err = new SplitStream(System.err);
        System.setOut(out);
        System.setErr(err);
        if (startargs.length != 0) {
            for (String arg : startargs) {
                if (arg.equalsIgnoreCase("-debugmode")) {
                    log("Starting with DEBUG MODE ENABLED");
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
            logSevere("An error occurred", ex);
        }
        synchronized (instance) {
            try {
                instance.wait();
            } catch (InterruptedException ex) {
                logSevere("The instance was interrupted", ex);
            }
        }

        eventHandler.stopRunner();

        Set<Channel> chans = driver.getChannels();
        Channel[] chanArray = chans.toArray(new Channel[0]);
        String[] chanNames = new String[chans.size()];
        for (int i = 0; i < chanArray.length; i++) {
            chanNames[i] = chanArray[i].getName();
        }

        for (String chan : chanNames) {
            try {
                Channel c = driver.getChannel(chan);
                if (driver.getChannels().contains(c)) {
                    driver.partChannel(c, "Exiting channel");
                }
            } catch (Exception ex) {
                logSevere("An error occured on shutting down", ex);
            }
        }

        try {
            driver.quitServer();
        } catch (Exception ex) {
            logSevere("An error occured on shutting down", ex);
        }
        try {
            driver.disconnect();
        } catch (Exception ex) {
            logSevere("An error occured on shutting down", ex);

        }
        try {
            driver.shutdown(false);
        } catch (Exception ex) {
            logSevere("An error occured on shutting down", ex);
        }
        System.exit(0);
    }

    private void createInstance(String user, String pass) throws IOException, IrcException {
        String bind = Settings.getGlobalSettings().getString("bind-ip");
        if (bind != null && !bind.isEmpty()) {
            InetAddress addr = InetAddress.getByName(bind);
            driver.setInetAddress(addr);
        }
        driver.setVerbose(true);
        driver.setVersion("RalexBot - v" + VERSION);
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

        log("Nick of bot: " + nick);

        Utilities.setUtils(driver);

        eventHandler.load();
        boolean eventSuccess = driver.getListenerManager().addListener(eventHandler);
        if (eventSuccess) {
            log("Listener hook attached to bot");
        } else {
            log("Listener hook was unable to attach to the bot");
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
        log("Connecting to: " + network + ":" + port);
        try {
            driver.connect(network, port);
        } catch (NickAlreadyInUseException ex) {
            logSevere("The nick is already taken");
            driver.changeNick(nick + "_");
            driver.connect(network, port);
            driver.sendMessage("chanserv", "ghost " + nick + " " + pass);
            driver.changeNick(nick);
            if (!globalSettings.getString("nick").equalsIgnoreCase(driver.getNick())) {
                logSevere("Could not claim the nick " + nick);
            }
        }
        BotUser bot = BotUser.getBotUser();
        if (pass != null && !pass.isEmpty() && login) {
            bot.sendMessage("nickserv", "identify " + pass);
            log("Logging in to nickserv");
        }
        eventHandler.fireEvent(new ConnectionEvent());
        List<String> channels = globalSettings.getStringList("channels");
        if (channels != null && !channels.isEmpty()) {
            for (String chan : channels) {
                log("Joining " + chan);
                bot.joinChannel(chan);
            }
        } else {
            bot.joinChannel("#ae97");
        }
        log("Initial loading complete, engaging listeners");
        eventHandler.startQueue();
        log("Starting keyboard listener");
        kblistener.start();
        log("All systems operational");
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

    public static void copyInputStream(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException ex) {
            logSevere("An error occurred on copying the streams", ex);
        }
    }

    public static void log(String message) {
        System.out.println(message);
    }

    public static void log(String message, Throwable error) {
        log(message);
        error.printStackTrace(System.out);
    }

    public static void logSevere(String message) {
        System.err.println("[SEVERE] " + message);
    }

    public static void logSevere(String message, Throwable error) {
        logSevere(message);
        error.printStackTrace(System.err);
    }

    public static PermissionManager getPermManager() {
        return permManager;
    }

    private RalexBot() {
    }

    private static class SplitStream extends PrintStream {

        private final PrintStream first, second;

        public SplitStream(PrintStream f) throws FileNotFoundException {
            super(f);
            first = f;
            second = new PrintStream(new FileOutputStream("logs.log", true));
        }

        @Override
        public void write(byte[] b) throws IOException {
            first.write(b);
            second.write(b);
        }

        @Override
        public void write(int b) {
            first.write(b);
            second.write(b);
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            first.write(buf, off, len);
            second.write(buf, off, len);
        }

        @Override
        public void close() {
            first.close();
            second.close();
        }

        @Override
        public void flush() {
            first.flush();
            second.flush();
        }
    }
}
