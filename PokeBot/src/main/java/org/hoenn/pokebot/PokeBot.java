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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import jline.console.ConsoleReader;
import org.hoenn.pokebot.eventhandler.EventHandler;
import org.hoenn.pokebot.extension.ExtensionManager;
import org.hoenn.pokebot.permissions.PermissionManager;
import org.hoenn.pokebot.scheduler.Scheduler;
import org.hoenn.pokebot.settings.Settings;
import org.hoenn.pokebot.stream.SplitPrintStream;
import org.pircbotx.exception.IrcException;

public final class PokeBot extends Thread {

    private static PokeBotCore core;
    private static boolean debugMode = false;
    private static final Map<String, String> args = new HashMap<>();
    private static boolean login = true;
    public static final String VERSION = "6.0.0";

    public static void main(String[] startargs) throws IOException, IrcException {
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
        core = new PokeBotCore();
        core.createInstance(args.get("user"), args.get("pass"));
        synchronized (core) {
            try {
                core.wait();
            } catch (InterruptedException ex) {
                log(Level.SEVERE, "The instance was interrupted", ex);
            }
        }
        core.shutdown();
        System.exit(0);
    }

    public static EventHandler getEventHandler() {
        return core.getEventHandler();
    }

    public static ExtensionManager getExtensionManager() {
        return core.getExtensionManager();
    }

    public static Scheduler getScheduler() {
        return core.getScheduler();
    }

    public static ConsoleReader getConsole() {
        return core.getConsole();
    }

    public static PermissionManager getPermManager() {
        return core.getPermManager();
    }

    public static Settings getSettings() {
        return core.getSettings();
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
}
