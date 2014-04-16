/*
 * Copyright (C) 2014 Lord_Ralex
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
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocketFactory;
import jline.console.ConsoleReader;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.events.ConnectionEvent;
import org.hoenn.pokebot.api.users.Bot;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.configuration.InvalidConfigurationException;
import org.hoenn.pokebot.configuration.file.YamlConfiguration;
import org.hoenn.pokebot.eventhandler.EventHandler;
import org.hoenn.pokebot.extension.ExtensionManager;
import org.hoenn.pokebot.implementation.PokeBotBot;
import org.hoenn.pokebot.implementation.PokeBotChannel;
import org.hoenn.pokebot.implementation.PokeBotUser;
import org.hoenn.pokebot.implementation.PokeIrcBot;
import org.hoenn.pokebot.input.KeyboardListener;
import org.hoenn.pokebot.permissions.PermissionManager;
import org.hoenn.pokebot.scheduler.Scheduler;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.IdentServer;

/**
 *
 * @author Lord_Ralex
 */
public class PokeBotCore {

    private final EventHandler eventHandler;
    private final KeyboardListener kblistener;
    private final YamlConfiguration globalSettings;
    private final PermissionManager permManager;
    private final ExtensionManager extensionManager;
    private final Scheduler scheduler;
    private final PokeIrcBot driver;
    private final ConcurrentHashMap<String, Channel> channelCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<org.pircbotx.User, User> userCache = new ConcurrentHashMap<>();
    private final static Logger logger = Logger.getLogger("Pokebot");
    private Bot botUser;

    protected PokeBotCore() throws UnknownHostException {
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
                        getLogger().log(Level.SEVERE, "An error occurred on copying the streams", ex);
                    }
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error on saving config", ex);
            }
        }
        globalSettings = new YamlConfiguration();
        try {
            globalSettings.load(new File("config.yml"));
        } catch (IOException | InvalidConfigurationException ex) {
            logger.log(Level.SEVERE, "Failed to load config.yml", ex);
        }
        IdentServer.startServer();
        Builder<PokeIrcBot> botConfigBuilder = new Builder<PokeIrcBot>()
                .setEncoding(Charset.forName("UTF-8"))
                .setVersion("PokeBot - v" + PokeBot.VERSION)
                .setAutoReconnect(true)
                .setAutoNickChange(true)
                .setIdentServerEnabled(true)
                .setName(globalSettings.getString("nick", "DebugBot"))
                .setLogin(globalSettings.getString("nick", "DebugBot"))
                .setRealName(globalSettings.getString("nick", "DebugBot"))
                .setNickservPassword(globalSettings.getString("nick-pw", ""))
                .setServerHostname(globalSettings.getString("network"))
                .setServerPort(globalSettings.getInt("port", 6667));
        if (globalSettings.getBoolean("ssl")) {
            botConfigBuilder.setSocketFactory(SSLSocketFactory.getDefault());
        }

        if (globalSettings.isString("bind-ip")) {
            botConfigBuilder.setLocalAddress(InetAddress.getByName(globalSettings.getString("bind-ip")));
        }
        if (globalSettings.isList("channels")) {
            for (String chan : globalSettings.getStringList("channels")) {
                botConfigBuilder.addAutoJoinChannel(chan);
            }
        }

        driver = new PokeIrcBot(botConfigBuilder.buildConfiguration());
        KeyboardListener temp;
        try {
            temp = new KeyboardListener(this, driver);
        } catch (IOException ex) {
            temp = null;
            logger.log(Level.SEVERE, "An error occured", ex);
        }
        kblistener = temp;
        eventHandler = new EventHandler(driver);
        extensionManager = new ExtensionManager();
        permManager = new PermissionManager();
        scheduler = new Scheduler();

        eventHandler.load();
        extensionManager.load();
        try {
            permManager.load();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading permissions file", e);
        }
        boolean eventSuccess = botConfigBuilder.getListenerManager().addListener(eventHandler);
        if (eventSuccess) {
            logger.log(Level.INFO, "Listener hook attached to bot");
        } else {
            logger.log(Level.INFO, "Listener hook was unable to attach to the bot");
        }
        eventHandler.fireEvent(new ConnectionEvent());

        logger.log(Level.INFO, "Initial loading complete, engaging listeners");
        eventHandler.startQueue();
        logger.log(Level.INFO, "Starting keyboard listener");
        kblistener.start();
        logger.log(Level.INFO, "All systems operational");
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

    public YamlConfiguration getSettings() {
        return globalSettings;
    }

    public void shutdown() {
        eventHandler.stopRunner();
    }

    public Channel getChannel(String name) {
        if (channelCache.containsKey(name.toLowerCase())) {
            return channelCache.get(name.toLowerCase());
        }
        Channel newChan = new PokeBotChannel(driver, name);
        channelCache.put(name.toLowerCase(), newChan);
        return newChan;
    }

    public User getUser(String name) {
        org.pircbotx.User pircbotxUser = driver.getUserChannelDao().getUser(name);
        if (userCache.contains(pircbotxUser)) {
            return userCache.get(pircbotxUser);
        }
        User newUser = new PokeBotUser(driver, name);
        userCache.put(pircbotxUser, newUser);
        return newUser;
    }

    public Bot getBot() {
        if (botUser == null) {
            botUser = new PokeBotBot(driver);
        }
        return botUser;
    }

    public static Logger getLogger() {
        return logger;
    }
}
