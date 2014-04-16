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
package org.hoenn.pokebot.extensions.faqlegacy;

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
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.CommandExecutor;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.events.CommandEvent;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.extension.Extension;
import org.hoenn.pokebot.extension.ExtensionReloadFailedException;

/**
 * @author Lord_Ralex
 */
public class FaqExtension extends Extension implements CommandExecutor {

    private final Map<String, Database> databases = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "Faq Extension";
    }

    @Override
    public void load() {
        loadDatabases();
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void reload() throws ExtensionReloadFailedException {
        super.reload();
        loadDatabases();
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
                    MessageTask thread = new MessageTask(event.getArgs()[1].toLowerCase(), target, channel, lines, false, target == null ? getConfig().getString("format-notarget") : getConfig().getString("format"), getConfig().getInt("delay"));
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
                    MessageTask thread = new MessageTask(event.getArgs()[1].toLowerCase(), target, channel, lines, true, target == null ? getConfig().getString("format-notarget") : getConfig().getString("format"), getConfig().getInt("delay"));
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
                    MessageTask thread = new MessageTask(event.getArgs()[1].toLowerCase(), target, channel, lines, true, target == null ? getConfig().getString("format-notarget") : getConfig().getString("format"), getConfig().getInt("delay"));
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
                    MessageTask thread = new MessageTask(event.getArgs()[0].toLowerCase(), target, channel, lines, true, target == null ? getConfig().getString("format-notarget") : getConfig().getString("format"), getConfig().getInt("delay"));
                    thread.start();
                }
                break;
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
                    MessageTask thread = new MessageTask(event.getArgs()[0].toLowerCase(), target, channel, lines, false, target == null ? getConfig().getString("format-notarget") : getConfig().getString("format"), getConfig().getInt("delay"));
                    thread.start();
                }
                break;
            }
        }
    }

    @Override
    public String[] getAliases() {
        ArrayList<String> aliases = new ArrayList<>(Arrays.asList(new String[]{
            "faq",
            "refresh",
            ">",
            "<",
            "<<",
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
        databases.clear();
        List<String> databasesToLoad = getConfig().getStringList("databases");
        for (String load : databasesToLoad) {
            try {
                Database database;
                String name = load;
                getLogger().log(Level.INFO, "Reading settings: " + name);
                switch (getConfig().getString(load + ".type")) {
                    case "FILE": {
                        String loadPath = getConfig().getString(load + ".load");
                        String savePath = getConfig().getString(load + ".path");
                        database = new FileDatabase(name, savePath);
                        database.setReadonly(getConfig().getBoolean(load + ".readonly"));
                        database.setMaster(getConfig().getString(load + ".owner"));
                        PokeBot.getLogger().log(Level.INFO, "    Creating database: " + database.getName());
                        PokeBot.getLogger().log(Level.INFO, "      Path to get info: " + loadPath);
                        PokeBot.getLogger().log(Level.INFO, "      Path to save info: " + ((FileDatabase) database).getFile());
                        PokeBot.getLogger().log(Level.INFO, "      Read-only: " + database.isReadonly());
                        PokeBot.getLogger().log(Level.INFO, "      Database owner: " + database.getMaster());
                        if (!loadPath.equals(((FileDatabase) database).getFile())) {
                            PokeBot.getLogger().log(Level.INFO, "        Downloading database: " + loadPath);
                            PokeBot.getLogger().log(Level.INFO, "        Saving to " + ((FileDatabase) database).getFile());
                            File save = new File(((FileDatabase) database).getFile());
                            save.delete();
                            save.getParentFile().mkdirs();
                            save.createNewFile();
                            FileOutputStream out = new FileOutputStream(save);
                            InputStream in = new URL(loadPath).openStream();
                            copyInputStream(in, out);
                            PokeBot.getLogger().log(Level.INFO, "        Downloaded: " + (save.length() / 1024) + "kb");
                            PokeBot.getLogger().log(Level.INFO, "        Installed: " + database.getName());
                        }
                        PokeBot.getLogger().log(Level.INFO, "    Loading database: " + database.getName());
                        database.load();
                        databases.put(database.getName().toLowerCase(), database);
                        break;
                    }
                }
            } catch (IOException | IndexOutOfBoundsException | NullPointerException ex) {
                PokeBot.getLogger().log(Level.SEVERE, "    There was an error with this setting: " + load, ex);
            }
        }
    }
}
