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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import jline.console.ConsoleReader;
import net.ae97.pircboty.ConsoleLogHandler;
import net.ae97.pircboty.FileLogHandler;
import net.ae97.pircboty.LoggerStream;
import net.ae97.pircboty.PircBotY;
import net.ae97.pircboty.PrefixLogger;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.users.Bot;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.configuration.file.YamlConfiguration;
import org.hoenn.pokebot.eventhandler.EventHandler;
import org.hoenn.pokebot.extension.ExtensionManager;
import org.hoenn.pokebot.permissions.PermissionManager;
import org.hoenn.pokebot.scheduler.Scheduler;

public final class PokeBot extends Thread {

    private static final PokeBotCore core;
    public static final String VERSION = "6.0.0";

    static {
        Logger logger = new PrefixLogger("Pokebot");
        PokeBotCore tempCore = null;
        try {
            for (Handler h : logger.getHandlers()) {
                logger.removeHandler(h);
            }
            logger.addHandler(new FileLogHandler("output.log"));
            logger.addHandler(new ConsoleLogHandler());
            PircBotY.getLogger().setParent(logger);
            LoggerStream out = new LoggerStream(System.out, logger, Level.INFO);
            LoggerStream err = new LoggerStream(System.err, logger, Level.SEVERE);
            System.setOut(out);
            System.setErr(err);
            tempCore = new PokeBotCore(logger);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error on creating core bot, cannot continue", e);
            System.exit(1);
        }
        core = tempCore;
    }

    public static void main(String[] startargs) {
        //IdentServer.startServer();
        core.start();
        synchronized (core) {
            try {
                core.wait();
            } catch (InterruptedException ex) {
                getLogger().log(Level.SEVERE, "The instance was interrupted", ex);
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

    public static YamlConfiguration getSettings() {
        return core.getSettings();
    }

    public static User getUser(String name) {
        return core.getUser(name);
    }

    public static Channel getChannel(String name) {
        return core.getChannel(name);
    }

    public static Bot getBot() {
        return core.getBot();
    }

    public static Logger getLogger() {
        return core.getLogger();
    }
}
