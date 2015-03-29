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
package net.ae97.pokebot.extensions.faq;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;
import net.ae97.pokebot.extension.ExtensionReloadFailedException;
import net.ae97.pokebot.extensions.faq.commands.FaqSubCommand;
import net.ae97.pokebot.extensions.faq.commands.MessageChannelCommand;
import net.ae97.pokebot.extensions.faq.commands.MessageSelfCommand;
import net.ae97.pokebot.extensions.faq.commands.MessageUserCommand;
import net.ae97.pokebot.extensions.faq.database.Database;
import net.ae97.pokebot.extensions.faq.database.FileDatabase;
import net.ae97.pokebot.extensions.faq.database.MySQLDatabase;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lord_Ralex
 */
public class FaqExtension extends Extension implements CommandExecutor {

    private final Map<String, Database> databases = new ConcurrentHashMap<>();
    private final Map<String, FaqSubCommand> subCommands = new ConcurrentHashMap<>();
    private int delay;
    private String messageFormat, messageFormat_nouser;
    private final List<Character> delimiters = new ArrayList<>();
    private final Stack<TimeKeeper> timeTracker = new Stack<>();
    private final int floodDelay = 10 * 1000;

    @Override
    public String getName() {
        return "Faq";
    }

    @Override
    public void load() {
        delay = getConfig().getInt("delay");
        messageFormat = getConfig().getString("format");
        messageFormat_nouser = getConfig().getString("format-nouser");
        List<String> delimits = getConfig().getStringList("delimiters");
        for (String str : delimits) {
            if (str.length() == 1) {
                delimiters.add(str.charAt(0));
            }
        }
        loadCommands();
        loadDatabases();
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void reload() throws ExtensionReloadFailedException {
        super.reload();
        delay = getConfig().getInt("delay");
        messageFormat = getConfig().getString("format");
        messageFormat_nouser = getConfig().getString("format-nouser");
        List<String> delimits = getConfig().getStringList("delimiters");
        for (String str : delimits) {
            if (str.length() == 1) {
                delimiters.add(str.charAt(0));
            }
        }
        loadCommands();
        loadDatabases();
    }

    @Override
    public void runEvent(CommandEvent event) {
        Database index = getDatabase(event);
        Iterator<TimeKeeper> keeper = timeTracker.iterator();
        long startTime = System.currentTimeMillis() - floodDelay;
        while (keeper.hasNext()) {
            if (keeper.next().getTime() < startTime) {
                keeper.remove();
            }
        }
        for (TimeKeeper time : timeTracker) {
            if (time.getCommand().equalsIgnoreCase(StringUtils.join(event.getArgs(), " "))) {
                event.getUser().send().notice("I already used this command within 10 seconds");
                return;
            }
        }
        timeTracker.add(new TimeKeeper(StringUtils.join(event.getArgs(), " ")));
        for (Entry<String, FaqSubCommand> entry : subCommands.entrySet()) {
            if (event.getCommand().equalsIgnoreCase(entry.getKey())) {
                entry.getValue().execute(this, event, index);
                return;
            }
        }
    }

    @Override
    public String[] getAliases() {
        ArrayList<String> aliases = new ArrayList<>();
        aliases.addAll(subCommands.keySet());
        String[] it;
        synchronized (databases) {
            it = databases.keySet().toArray(new String[0]);
        }
        for (String key : it) {
            for (String sub : subCommands.keySet()) {
                aliases.add(key + sub);
            }
        }
        return aliases.toArray(new String[aliases.size()]);
    }

    public Map<String, Database> getDatabases() {
        return databases;
    }

    public int getDelay() {
        return delay;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public String getMessageFormatNoUser() {
        return messageFormat_nouser;
    }

    public List<Character> getDelimiters() {
        return delimiters;
    }

    public void loadDatabases() {
        List<String> databasesToLoad = getConfig().getStringList("databases");
        for (String load : databasesToLoad) {
            String databaseType = getConfig().getString(load + ".type");
            try {
                Database newDatabase = null;
                String name = load;
                HashMap<String, String> details = new HashMap<>();
                switch (databaseType.toUpperCase()) {
                    case "MYSQL": {
                        details.put("host", getConfig().getString(name + ".host", "localhost"));
                        details.put("port", getConfig().getString(name + ".port", "3306"));
                        details.put("user", getConfig().getString(name + ".user", "root"));
                        details.put("pass", getConfig().getString(name + ".pass", ""));
                        details.put("database", getConfig().getString(name + ".database", "database"));
                        newDatabase = new MySQLDatabase(name, details);
                    }
                    break;
                    case "FLAT": {
                        details.put("load", getConfig().getString(name + ".load", new File("database", name + ".db").getPath()));
                        details.put("save", getConfig().getString(name + ".save", new File("database", name + ".db").getPath()));
                        details.put("update", getConfig().getString(name + ".update", "0"));
                        newDatabase = new FileDatabase(name, details);
                    }
                    break;
                }
                if (newDatabase != null) {
                    PokeBot.getLogger().log(Level.INFO, "[FAQ] Loading database: " + newDatabase.getName());
                    newDatabase.load();
                    databases.put(newDatabase.getName().toLowerCase(), newDatabase);
                } else {
                    PokeBot.getLogger().log(Level.WARNING, name + " could not be created into a database, no " + databaseType.toUpperCase() + " found");
                }
            } catch (SQLException ex) {
                PokeBot.getLogger().log(Level.SEVERE, "    There was an error with this setting: " + load, ex);
            }
        }
    }

    public void loadCommands() {
        List<FaqSubCommand> subs = new ArrayList<>();
        subs.add(new MessageChannelCommand());
        subs.add(new MessageSelfCommand());
        subs.add(new MessageUserCommand());
        for (FaqSubCommand cmd : subs) {
            for (String key : cmd.getPrefix()) {
                subCommands.put(key, cmd);
            }
        }
    }

    public Database getDatabase(CommandEvent event) {
        Database index = null;
        String[] dbNames = databases.keySet().toArray(new String[databases.size()]);
        for (String name : dbNames) {
            if (event.getCommand().contains(name)) {
                index = databases.get(name);
                break;
            } else if (event.getChannel() != null) {
                if (event.getChannel().getName().contains(name)) {
                    index = databases.get(name);
                    break;
                }
            }
        }
        if (databases.size() == 1) {
            index = databases.get(dbNames[0]);
        }
        return index;
    }

    public String[] splitFactoid(String line) {
        for (Character ch : delimiters) {
            if (line.contains(ch.toString())) {
                return line.split(ch.toString());
            }
        }
        return new String[]{line};
    }
}
