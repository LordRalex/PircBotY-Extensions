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

import net.ae97.ralexbot.EventHandler;
import net.ae97.ralexbot.RalexBot;
import net.ae97.ralexbot.api.EventField;
import net.ae97.ralexbot.api.EventType;
import net.ae97.ralexbot.api.Listener;
import net.ae97.ralexbot.api.events.CommandEvent;
import net.ae97.ralexbot.api.users.BotUser;
import net.ae97.ralexbot.data.DataType;
import static net.ae97.ralexbot.data.DataType.SQL;
import net.ae97.ralexbot.settings.Settings;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.pircbotx.Colors;

/**
 * @author Lord_Ralex
 * @version 1.0
 */
public class FaqSystem extends Listener {

    private final Map<String, Database> databases = new ConcurrentHashMap<>();
    private int delay = 2;
    private final ScheduledExecutorService es = Executors.newScheduledThreadPool(3);
    private Settings settings;

    @Override
    public void onLoad() {
        settings = new Settings(new File("settings", "faq.yml"));
        loadDatabases();
        delay = settings.getInt("delay");
    }

    @Override
    public void onUnload() {
        databases.clear();
        es.shutdown();
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.getCommand().equalsIgnoreCase("refresh")) {
            loadDatabases();
            event.getUser().sendMessage("Updated local storage of all databases");
            return;
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
                    List<String> users = event.getChannel().getUsers();
                    for (String u : users) {
                        if (u.equalsIgnoreCase(master)) {
                            allowExec = false;
                        }
                    }
                }
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
            switch (cmdMethod) {
                case ">": {
                    String target = event.getArgs()[0];
                    String channel = event.getChannel().getName();
                    String[] lines = index.getEntry(event.getArgs()[1].toLowerCase());
                    if (lines == null || lines.length == 0) {
                        event.getUser().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[1] + " in the database");
                        return;
                    }
                    RunLaterThread thread = new RunLaterThread(event.getArgs()[1].toLowerCase(), target, channel, lines, false);
                    thread.setFuture(es.scheduleWithFixedDelay(thread, 1, delay, TimeUnit.SECONDS));
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
                    RunLaterThread thread = new RunLaterThread(event.getArgs()[1].toLowerCase(), target, channel, lines, true);
                    thread.setFuture(es.scheduleWithFixedDelay(thread, 1, delay, TimeUnit.SECONDS));
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
                    RunLaterThread thread = new RunLaterThread(event.getArgs()[1].toLowerCase(), target, channel, lines, true);
                    thread.setFuture(es.scheduleWithFixedDelay(thread, 1, delay, TimeUnit.SECONDS));
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
                    RunLaterThread thread = new RunLaterThread(event.getArgs()[0].toLowerCase(), target, channel, lines, true);
                    thread.setFuture(es.scheduleWithFixedDelay(thread, 1, delay, TimeUnit.SECONDS));
                }
                break;
                case "+": {
                    String loginName = event.getUser().isVerified();
                    if (loginName == null || !index.canEdit(loginName)) {
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
                    event.getUser().sendNotice("The factoid " + event.getArgs()[0].toLowerCase() + " has been added");
                }
                break;
                case "-": {
                    String loginName = event.getUser().isVerified();
                    if (loginName == null || !index.canRemove(loginName)) {
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
                    String loginName = event.getUser().isVerified();
                    if (loginName == null || !index.canEdit(loginName)) {
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
                default: {
                    String target = null;
                    String channel = event.getChannel().getName();
                    String[] lines = index.getEntry(event.getArgs()[0].toLowerCase());
                    if (lines == null || lines.length == 0) {
                        event.getUser().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[0] + " in the database");
                        return;
                    }
                    RunLaterThread thread = new RunLaterThread(event.getArgs()[0].toLowerCase(), target, channel, lines, false);
                    thread.setFuture(es.scheduleWithFixedDelay(thread, 1, delay, TimeUnit.SECONDS));
                }
                break;
            }
            if (databaseChanged) {
                try {
                    saveDatabase(index);
                } catch (IOException ex) {
                    RalexBot.logSevere("An error occured on saving the database " + index.getName(), ex);
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
            "~"}));
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
        }
        return aliases.toArray(new String[aliases.size()]);
    }

    private void copyInputStream(InputStream in, FileOutputStream out) throws IOException {
        ReadableByteChannel rbc = Channels.newChannel(in);
        out.getChannel().transferFrom(rbc, 0, 1 << 24);
    }

    private synchronized void loadDatabases() {
        List<String> databasesToLoad = settings.getStringList("databases");
        for (String load : databasesToLoad) {
            try {
                String name = load.split(" ")[0].toLowerCase();
                String loadPath = load.split(" ")[1];
                String savePath = load.split(" ")[2];
                Database newDatabase = new Database(name, savePath, DataType.FLAT);
                if (load.split(" ").length <= 4) {
                    newDatabase.setReadonly(Boolean.parseBoolean(load.split(" ")[3]));
                }
                if (load.split(" ").length <= 5) {
                    newDatabase.setMaster(load.split(" ")[4]);
                }
                RalexBot.log("Creating database: " + newDatabase.getName());
                RalexBot.log("  Path to get info: " + loadPath);
                RalexBot.log("  Path to save info: " + newDatabase.getFile());
                RalexBot.log("  Read-only: " + newDatabase.isReadonly());
                RalexBot.log("  Database owner: " + newDatabase.getMaster());
                List<String> addable = settings.getStringList("faq-database-add-" + newDatabase.getName());
                for (String n : addable) {
                    newDatabase.addAddable(n);
                }
                List<String> editable = settings.getStringList("faq-database-edit-" + newDatabase.getName());
                for (String n : editable) {
                    newDatabase.addEditable(n);
                }
                List<String> removeable = settings.getStringList("faq-database-remove-" + newDatabase.getName());
                for (String n : removeable) {
                    newDatabase.addRemoveable(n);
                }
                if (!loadPath.equals(newDatabase.getFile())) {
                    RalexBot.log("  Downloading database: " + loadPath);
                    RalexBot.log("  Saving to " + newDatabase.getFile());
                    File save = new File(newDatabase.getFile());
                    save.delete();
                    save.getParentFile().mkdirs();
                    save.createNewFile();
                    FileOutputStream out = new FileOutputStream(save);
                    InputStream in = new URL(loadPath).openStream();
                    copyInputStream(in, out);
                    RalexBot.log("  Downloaded: " + (save.length() / 1024) + "kb");
                    RalexBot.log("  Installed: " + newDatabase.getName());
                }
                RalexBot.log("  Loading database: " + newDatabase.getName());
                newDatabase.load(newDatabase.getFile());
                databases.put(newDatabase.getName().toLowerCase(), newDatabase);
            } catch (Exception ex) {
                RalexBot.logSevere("There was an error with this setting: " + load, ex);
            }
        }
    }

    private synchronized void saveDatabase(Database db) throws IOException {
        db.save();
    }

    private class RunLaterThread implements Runnable {

        private List<String> lines;
        private String channel;
        private String user;
        private boolean notice = false;
        private String name;
        private ScheduledFuture future = null;

        public RunLaterThread(String na, String u, String c, String[] l, boolean n) {
            lines = new ArrayList<>(Arrays.asList(l));
            channel = c;
            user = u;
            notice = n;
            name = na;
        }

        @Override
        public void run() {
            if (lines.isEmpty()) {
                if (future != null) {
                    future.cancel(true);
                }
                return;
            }
            BotUser bot = BotUser.getBotUser();
            String message = lines.remove(0);
            if (user == null) {
                message = Colors.BOLD + name.toLowerCase() + ": " + Colors.NORMAL + message;
            } else {
                message = Colors.BOLD + user + ": " + Colors.NORMAL + "(" + name.toLowerCase() + ") " + message;
            }
            if (notice) {
                bot.sendNotice(user, message);
            } else {
                bot.sendMessage(channel, message);
            }
            if (lines.isEmpty()) {
                if (future != null) {
                    future.cancel(true);
                }
            }
        }

        public void setFuture(ScheduledFuture sf) {
            future = sf;
        }
    }

    private class Database {

        private final String location;
        private final Map<String, String[]> factoids = new ConcurrentHashMap<>();
        private String master = null;
        private boolean readOnly = false;
        private final String name;
        private final Set<String> add = new HashSet<>();
        private final Set<String> remove = new HashSet<>();
        private final Set<String> edit = new HashSet<>();
        private final DataType storage;

        public Database(String n, String filePath, DataType store) {
            location = filePath;
            name = n;
            storage = store;
        }

        public void load(String loadPath) {
            switch (storage) {
                case FLAT:
                    try {
                        if (!loadPath.equals(location)) {
                            InputStream reader = new URL(loadPath).openStream();
                            FileOutputStream writer = new FileOutputStream(new File(location));
                            copyInputStream(reader, writer);
                        }
                        BufferedReader filereader = new BufferedReader(new FileReader(new File(location)));
                        String line;
                        while ((line = filereader.readLine()) != null) {
                            if (line.contains("|")) {
                                String key = line.split("\\|")[0];
                                String value = line.split("\\|", 2)[1];
                                setEntry(key.toLowerCase(), value.split(";;"));
                            }
                        }
                    } catch (IOException ex) {
                        RalexBot.logSevere("There was an error", ex);
                    }
                    break;
                case SQL:
                    try {
                        //storage.load();
                        throw new IOException("SQL is not set up yet");
                    } catch (IOException e) {
                        RalexBot.logSevere("There was an error", e);
                    }
                    break;
            }
        }

        public String getName() {
            return name;
        }

        public void setMaster(String m) {
            master = m;
        }

        public String getMaster() {
            return master;
        }

        public void setReadonly(boolean newBool) {
            readOnly = newBool;
        }

        public boolean isReadonly() {
            return readOnly;
        }

        public String[] getEntry(String key) {
            String[] entry;
            synchronized (factoids) {
                entry = factoids.get(key);
            }
            /*if (entry == null) {
             switch (storage.getType()) {
             case SQL: {
             try {
             PreparedStatement statement = ((MySQLConnection) storage).getPreparedStatement("SELECT ? FROM ? WHERE ID=?", location, ((MySQLConnection) storage).getTable(), key);
             ResultSet set = statement.executeQuery();
             String result = set.getString(1);
             entry = result.split(";;");
             setEntry(key, entry);
             } catch (SQLException ex) {
             RalexBot.logSevere("An error occured", ex);
             }
             }
             break;
             }

             }*/
            return entry;
        }

        public boolean setEntry(String key, String[] newEntry) {
            synchronized (factoids) {
                factoids.put(key, newEntry);
            }
            /*switch (storage.getType()) {
             case SQL: {
             try {
             PreparedStatement statement = ((MySQLConnection) storage).getPreparedStatement("UPDATE ? FROM ? WHERE ID=?", location, ((MySQLConnection) storage).getTable(), key);
             statement.execute();
             } catch (SQLException ex) {
             RalexBot.logSevere("An error occured", ex);
             }
             }
             break;
             }*/
            return true;
        }

        public boolean removeEntry(String key) {
            boolean removed;
            synchronized (factoids) {
                removed = factoids.remove(key) != null;
            }
            return removed;
        }

        public String getFile() {
            return location;
        }

        public synchronized void save() throws IOException {
            switch (storage) {
                case FLAT: {
                    new File(location).mkdirs();
                    new File(location).delete();
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(location)))) {
                        synchronized (factoids) {
                            Set<String> keys = factoids.keySet();
                            for (String key : keys) {
                                String line = key + "|";
                                String[] parts = factoids.get(key);
                                for (int i = 0; i < parts.length; i++) {
                                    if (i != 0) {
                                        line += ";;";
                                    }
                                    line += parts[i];
                                    writer.write(line);
                                    writer.newLine();
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }

        public boolean canEdit(String login) {
            if (login == null) {
                return false;
            }
            return edit.contains(login.toLowerCase());
        }

        public boolean canAdd(String login) {
            if (login == null) {
                return false;
            }
            return add.contains(login.toLowerCase());
        }

        public boolean canRemove(String login) {
            if (login == null) {
                return false;
            }
            return remove.contains(login.toLowerCase());
        }

        public void addAddable(String n) {
            add.add(n.toLowerCase());
        }

        public void addEditable(String n) {
            edit.add(n.toLowerCase());
        }

        public void addRemoveable(String n) {
            remove.add(n.toLowerCase());
        }
    }
}
