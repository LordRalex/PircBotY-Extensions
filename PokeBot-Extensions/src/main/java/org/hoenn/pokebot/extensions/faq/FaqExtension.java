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
package org.hoenn.pokebot.extensions.faq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hoenn.pokebot.eventhandler.EventHandler;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.CommandExecutor;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.events.CommandEvent;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.extension.Extension;
import org.hoenn.pokebot.settings.Settings;

/**
 * @author Lord_Ralex
 */
public class FaqExtension extends Extension implements CommandExecutor {

    private final Map<String, Database> databases = new ConcurrentHashMap<>();
    private int delay;
    private Settings settings;
    private String messageFormat, messageFormat_nouser;

    @Override
    public void load() {
        settings = new Settings();
        try {
            settings.load(new File("config", "faq.yml"));
        } catch (IOException ex) {
            Logger.getLogger(FaqExtension.class.getName()).log(Level.SEVERE, null, ex);
        }
        delay = settings.getInt("delay");
        messageFormat = settings.getString("format");
        messageFormat_nouser = settings.getString("format-notarget");
        loadDatabases();
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (event.getCommand().equalsIgnoreCase("refresh")) {
            load();
            event.getUser().sendNotice("Updated local storage of all databases");
        } else if (event.getCommand().equalsIgnoreCase("togglefaq")) {
            Channel chan = event.getChannel();
            User user = event.getUser();
            if (chan.hasOp(user.getNick()) || user.hasPermission(chan.getName(), "fag.toggle")) {
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
                if (index == null) {
                    StringBuilder builder = new StringBuilder();
                    if (dbNames.length != 0) {
                        builder.append(dbNames[0]);
                    }
                    user.sendNotice("There is no FAQ that I could match this to");
                } else {
                    boolean oldState = index.getOverride();
                    index.setOverride(!oldState);
                    if (oldState) {
                        user.sendNotice("I am disabling my override on the database " + index.getName());
                    } else {
                        user.sendNotice("I am overriding my disable on the database " + index.getName());
                    }
                }
            }
        } else {
            boolean allowExec = true;
            String cmdMethod = event.getCommand().toLowerCase();
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
            if (index == null) {
                allowExec = false;
            } else {
                String master = index.getMaster();
                if (master == null) {
                    allowExec = true;
                } else {
                    String[] users = event.getChannel().getUserList();
                    for (String u : users) {
                        if (u.equalsIgnoreCase(master)) {
                            allowExec = false;
                        }
                    }
                }
            }
            if (cmdMethod.endsWith("^")) {
                allowExec = true;
            }
            if (!allowExec) {
                return;
            }
            if (index == null) {
                event.getUser().sendNotice("No database is selected");
                return;
            }
            boolean databaseChanged = false;
            for (String dbName : dbNames) {
                cmdMethod = cmdMethod.replace(dbName, "");
            }
            if (cmdMethod.endsWith("^")) {
                cmdMethod = cmdMethod.replace("^", "");
            }
            switch (cmdMethod) {
                case ">": {
                    String target = event.getArgs()[0];
                    String channel = event.getChannel().getName();
                    String[] lines = index.getEntry(event.getArgs()[1].toLowerCase());
                    if (lines == null || lines.length == 0) {
                        event.getUser().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[1] + " in the database");
                        return;
                    }
                    if (lines[0].equalsIgnoreCase("@deprecated")) {
                        event.getUser().sendNotice("The factoid " + event.getArgs()[1].toLowerCase() + " has been deprecated");
                        for (int i = 1; i < lines.length; i++) {
                            event.getUser().sendNotice(lines[i]);
                        }
                    }
                    MessageTask thread = new MessageTask(event.getArgs()[1].toLowerCase(), target, channel, lines, false, target == null ? messageFormat_nouser : messageFormat, delay);
                    thread.start();
                }
                break;
                case ">>": {
                    String target = event.getArgs()[0];
                    String channel = event.getChannel().getName();
                    String[] lines = index.getEntry(event.getArgs()[1].toLowerCase());
                    if (lines == null || lines.length == 0) {
                        event.getUser().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[1] + " in the database");
                        return;
                    }
                    if (lines[0].equalsIgnoreCase("@deprecated")) {
                        event.getUser().sendNotice("The factoid " + event.getArgs()[1].toLowerCase() + " has been deprecated");
                        for (int i = 1; i < lines.length; i++) {
                            event.getUser().sendNotice(lines[i]);
                        }
                    }
                    MessageTask thread = new MessageTask(event.getArgs()[1].toLowerCase(), target, channel, lines, true, target == null ? messageFormat_nouser : messageFormat, delay);
                    thread.start();
                }
                break;
                case "<<": {
                    String target = event.getArgs()[0];
                    String channel = event.getChannel().getName();
                    String[] lines = index.getEntry(event.getArgs()[1].toLowerCase());
                    if (lines == null || lines.length == 0) {
                        event.getUser().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[1] + " in the database");
                        return;
                    }
                    if (lines[0].equalsIgnoreCase("@deprecated")) {
                        event.getUser().sendNotice("The factoid " + event.getArgs()[1].toLowerCase() + " has been deprecated");
                        for (int i = 1; i < lines.length; i++) {
                            event.getUser().sendNotice(lines[i]);
                        }
                    }
                    MessageTask thread = new MessageTask(event.getArgs()[1].toLowerCase(), target, channel, lines, true, target == null ? messageFormat_nouser : messageFormat, delay);
                    thread.start();
                }
                break;

                case "<": {
                    String target = event.getUser().getNick();
                    String channel = event.getChannel().getName();
                    String[] lines = index.getEntry(event.getArgs()[0].toLowerCase());
                    if (lines == null || lines.length == 0) {
                        event.getUser().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[0] + " in the database");
                        return;
                    }
                    if (lines[0].equalsIgnoreCase("@deprecated")) {
                        event.getUser().sendNotice("The factoid " + event.getArgs()[0].toLowerCase() + " has been deprecated");
                        for (int i = 1; i < lines.length; i++) {
                            event.getUser().sendNotice(lines[i]);
                        }
                    }
                    MessageTask thread = new MessageTask(event.getArgs()[0].toLowerCase(), target, channel, lines, true, target == null ? messageFormat_nouser : messageFormat, delay);
                    thread.start();
                }
                break;
                case "+": {
                    if (!event.getUser().hasPermission((String) null, "faq.add." + index.getName())) {
                        event.getUser().sendNotice("You do not have permission to use this command");
                        return;
                    }
                    if (index.isReadonly()) {
                        event.getUser().sendNotice("The " + index.getName() + " FAQ database is read-only");
                        break;
                    }
                    if (event.getArgs().length < 2) {
                        event.getUser().sendNotice("Command usage: " + EventHandler.getCommandPrefixes().get(0) + "+ [factoid] [message]");
                        break;
                    }
                    String message = "";
                    for (int i = 1; i < event.getArgs().length; i++) {
                        message += event.getArgs()[i] + " ";
                    }
                    message = message.trim();
                    String[] faq = message.split(";;");
                    index.setEntry(event.getArgs()[0].toLowerCase(), faq);
                    databaseChanged = true;
                    event.getUser().sendNotice("The factoid " + event.getArgs()[0].toLowerCase() + " has been added");
                }
                break;
                case "-": {
                    if (!event.getUser().hasPermission((String) null, "faq.remove." + index.getName())) {
                        event.getUser().sendNotice("You do not have permission to use this command");
                        break;
                    }
                    if (index.isReadonly()) {
                        event.getUser().sendNotice("The " + index.getName() + " FAQ database is read-only");
                        break;
                    }
                    if (event.getArgs().length != 1) {
                        event.getUser().sendNotice("Command usage: " + EventHandler.getCommandPrefixes().get(0) + "- [factoid]");
                        break;
                    }
                    if (index.removeEntry(event.getArgs()[0].toLowerCase())) {
                        databaseChanged = true;
                        event.getUser().sendNotice("The factoid " + event.getArgs()[0].toLowerCase() + " has been removed");
                    } else {
                        event.getUser().sendNotice("The factoid " + event.getArgs()[0].toLowerCase() + " does not exist");
                    }
                }
                break;
                case "~": {
                    if (!event.getUser().hasPermission((String) null, "faq.edit." + index.getName())) {
                        event.getUser().sendNotice("You do not have permission to use this command");
                        break;
                    }
                    if (index.isReadonly()) {
                        event.getUser().sendNotice("The " + index.getName() + " FAQ database is read-only");
                        break;
                    }
                    if (event.getArgs().length < 2) {
                        event.getUser().sendNotice("Command usage: " + EventHandler.getCommandPrefixes().get(0) + "+ [factoid] [message]");
                        break;
                    }
                    String message = "";
                    for (int i = 1; i < event.getArgs().length; i++) {
                        message += event.getArgs()[i] + " ";
                    }
                    message = message.trim();
                    String[] faq = message.split(";;");
                    index.setEntry(event.getArgs()[0].toLowerCase(), faq);
                    databaseChanged = true;
                    event.getUser().sendNotice("The factoid " + event.getArgs()[0].toLowerCase() + " has been modified");
                }
                break;
                case "~replace": {
                    if (!event.getUser().hasPermission((String) null, "faq.edit." + index.getName())) {
                        return;
                    }
                    if (index.isReadonly()) {
                        event.getUser().sendNotice("The " + index.getName() + " FAQ database is read-only");
                        break;
                    }
                    if (event.getArgs().length < 2) {
                        event.getUser().sendNotice("Command usage: " + EventHandler.getCommandPrefixes().get(0) + "+ [factoid] [original] [replace with]");
                        event.getUser().sendNotice("You can use ' ' to seperate the arguments");
                        break;
                    }
                    StringBuilder builder = new StringBuilder();
                    for (String arg : event.getArgs()) {
                        builder.append(arg);
                        builder.append(" ");
                    }
                    String[] args = builder.toString().split("'");
                    String original = args[0];
                    String replacement = args[2];
                    String[] factoid = index.getEntry(event.getArgs()[0]);
                    for (int i = 0; i < factoid.length; i++) {
                        factoid[i] = factoid[i].replaceAll(original, replacement);
                    }
                    index.setEntry(event.getArgs()[0], factoid);
                    break;
                }
                default: {
                    String target = null;
                    String channel = event.getChannel().getName();
                    String[] lines = index.getEntry(event.getArgs()[0].toLowerCase());
                    if (lines == null || lines.length == 0) {
                        event.getUser().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[0] + " in the database");
                        return;
                    }
                    if (lines[0].equalsIgnoreCase("@deprecated")) {
                        event.getUser().sendNotice("The factoid " + event.getArgs()[0].toLowerCase() + " has been deprecated");
                        for (int i = 1; i < lines.length; i++) {
                            event.getUser().sendNotice(lines[i]);
                        }
                    }
                    MessageTask thread = new MessageTask(event.getArgs()[0].toLowerCase(), target, channel, lines, false, target == null ? messageFormat_nouser : messageFormat, delay);
                    thread.start();
                }
                break;
            }
            if (databaseChanged) {
                try {
                    saveDatabase(index);
                } catch (IOException ex) {
                    PokeBot.log(Level.SEVERE, "An error occured on saving the database " + index.getName(), ex);
                }
            }
        }
    }

    @Override
    public String[] getAliases() {
        ArrayList<String> aliases = new ArrayList(Arrays.asList(new String[]{
            "faq",
            "refresh",
            ">",
            "<",
            "<<",
            "+",
            "-",
            "~",
            "~replace",
            "^",
            "togglefaq",
            ""}));
        String[] it;
        synchronized (databases) {
            it = databases.keySet().toArray(new String[0]);
        }
        for (String key : it) {
            aliases.add(key + "");
            aliases.add(key + ">");
            aliases.add(key + "<<");
            aliases.add(key + "<");
            aliases.add(key + "+");
            aliases.add(key + "-");
            aliases.add(key + "~");
            aliases.add(key + "^");
        }
        return aliases.toArray(new String[aliases.size()]);
    }

    private void copyInputStream(InputStream in, FileOutputStream out) throws IOException {
        ReadableByteChannel rbc = Channels.newChannel(in);
        out.getChannel().transferFrom(rbc, 0, 1 << 24);
    }

    private void loadDatabases() {
        List<String> databasesToLoad = settings.getStringList("databases");
        for (String load : databasesToLoad) {
            try {
                String name = load;
                String loadPath = settings.getString(load + ".load");
                String savePath = settings.getString(load + ".path");
                Database newDatabase = new Database(name, savePath);
                newDatabase.setReadonly(settings.getBoolean(load + ".readonly"));
                newDatabase.setMaster(settings.getString(load + ".owner"));
                PokeBot.log(Level.INFO, "    Creating database: " + newDatabase.getName());
                PokeBot.log(Level.INFO, "      Path to get info: " + loadPath);
                PokeBot.log(Level.INFO, "      Path to save info: " + newDatabase.getFile());
                PokeBot.log(Level.INFO, "      Read-only: " + newDatabase.isReadonly());
                PokeBot.log(Level.INFO, "      Database owner: " + newDatabase.getMaster());
                if (!loadPath.equals(newDatabase.getFile())) {
                    PokeBot.log(Level.INFO, "        Downloading database: " + loadPath);
                    PokeBot.log(Level.INFO, "        Saving to " + newDatabase.getFile());
                    File save = new File(newDatabase.getFile());
                    save.delete();
                    save.getParentFile().mkdirs();
                    save.createNewFile();
                    FileOutputStream out = new FileOutputStream(save);
                    InputStream in = new URL(loadPath).openStream();
                    copyInputStream(in, out);
                    PokeBot.log(Level.INFO, "        Downloaded: " + (save.length() / 1024) + "kb");
                    PokeBot.log(Level.INFO, "        Installed: " + newDatabase.getName());
                }
                PokeBot.log(Level.INFO, "    Loading database: " + newDatabase.getName());
                newDatabase.load();
                databases.put(newDatabase.getName().toLowerCase(), newDatabase);
            } catch (IOException | IndexOutOfBoundsException | NullPointerException ex) {
                PokeBot.log(Level.SEVERE, "    There was an error with this setting: " + load, ex);
            }
        }
    }

    private void saveDatabase(Database db) throws IOException {
        synchronized (db) {
            db.save();
        }
    }
}
