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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.ae97.ralexbot.api.channels.Channel;
import net.ae97.ralexbot.api.users.User;
import net.ae97.ralexbot.mysql.MySQLConnection;
import org.pircbotx.Colors;

/**
 * @author Lord_Ralex
 * @version 1.0
 */
public class FaqSystem extends Listener {

    private final Map<String, Database> databases = new ConcurrentHashMap<>();
    private int delay = 2;
    private ScheduledExecutorService es;
    private Settings settings;

    @Override
    public void onLoad() {
        settings = new Settings(new File("settings", "faq.yml"));
        loadDatabases();
        delay = settings.getInt("delay");
        es = Executors.newScheduledThreadPool(10);
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
            event.getUser().sendNotice("Updated local storage of all databases");
            return;
        } else if (event.getCommand().equalsIgnoreCase("togglefaq")) {
            Channel chan = event.getChannel();
            User user = event.getUser();
            if (user.hasOP(chan.getName()) || user.hasPermission(chan.getName(), "fag.toggle")) {
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
                    List<String> users = event.getChannel().getUsers();
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
                    if (!event.getUser().hasPermission((String) null, "faq.add." + index.getName())) {
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
                        return;
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
            "~",
            "^",
            "togglefaq"}));
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

    private synchronized void loadDatabases() {
        List<String> databasesToLoad = settings.getStringList("databases");
        for (String load : databasesToLoad) {
            try {
                switch (load.split(" ")[0]) {
                    case "FLAT": {
                        String name = load.split(" ")[1].toLowerCase();
                        String loadPath = load.split(" ")[2];
                        String savePath = load.split(" ")[3];
                        Database newDatabase = new Database(name, savePath, DataType.FLAT);
                        if (load.split(" ").length <= 5) {
                            newDatabase.setReadonly(Boolean.parseBoolean(load.split(" ")[4]));
                        }
                        if (load.split(" ").length <= 6) {
                            newDatabase.setMaster(load.split(" ")[5]);
                        }
                        RalexBot.log("    Creating database: " + newDatabase.getName());
                        RalexBot.log("      Path to get info: " + loadPath);
                        RalexBot.log("      Path to save info: " + newDatabase.getFile());
                        RalexBot.log("      Read-only: " + newDatabase.isReadonly());
                        RalexBot.log("      Database owner: " + newDatabase.getMaster());
                        if (!loadPath.equals(newDatabase.getFile())) {
                            RalexBot.log("        Downloading database: " + loadPath);
                            RalexBot.log("        Saving to " + newDatabase.getFile());
                            File save = new File(newDatabase.getFile());
                            save.delete();
                            save.getParentFile().mkdirs();
                            save.createNewFile();
                            FileOutputStream out = new FileOutputStream(save);
                            InputStream in = new URL(loadPath).openStream();
                            copyInputStream(in, out);
                            RalexBot.log("        Downloaded: " + (save.length() / 1024) + "kb");
                            RalexBot.log("        Installed: " + newDatabase.getName());
                        }
                        RalexBot.log("    Loading database: " + newDatabase.getName());
                        newDatabase.load(newDatabase.getFile());
                        databases.put(newDatabase.getName().toLowerCase(), newDatabase);
                    }
                    break;
                    case "SQL": {
                        String name = load.split(" ")[1].toLowerCase();
                        String details = load.split(" ")[2];
                        Database newDatabase = new Database(name, details, DataType.SQL);
                        if (load.split(" ").length == 4) {
                            newDatabase.setMaster(load.split(" ")[3]);
                        }
                        newDatabase.load(details);
                        databases.put(newDatabase.getName().toLowerCase(), newDatabase);
                    }
                    break;
                }
            } catch (Exception ex) {
                RalexBot.logSevere("    There was an error with this setting: " + load, ex);
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
        private final DataType storage;
        private boolean override = false;

        public Database(String n, String path, DataType store) {
            location = path;
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
                        RalexBot.log("Path loading from: " + loadPath);
                        RalexBot.log("Location: " + location);
                    }
                    break;
                case SQL:
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
            if (override) {
                return null;
            }
            return master;
        }

        public void setOverride(boolean newState) {
            override = newState;
        }

        public boolean getOverride() {
            return override;
        }

        public void setReadonly(boolean newBool) {
            readOnly = newBool;
        }

        public boolean isReadonly() {
            return readOnly;
        }

        public String[] getEntry(String key) {
            String[] entry;
            key = key.toLowerCase().trim();
            synchronized (factoids) {
                entry = factoids.get(key);
            }
            if (entry == null) {
                switch (storage) {
                    case SQL: {
                        try {
                            String[] details = location.split(",");
                            String host = details[0];
                            String port = details[1];
                            String user = details[2];
                            String pass = details[3];
                            String db = details[4];
                            String table = details[5];
                            String column = details[6];
                            MySQLConnection conn = new MySQLConnection(host, Integer.parseInt(port), user, pass, db, table);
                            conn.load();
                            conn.getPreparedStatement("USE scrolls").execute();
                            PreparedStatement statement = conn.getPreparedStatement("SELECT " + column
                                    + " FROM " + ((MySQLConnection) conn).getTable() + " WHERE name='" + key + "';");
                            ResultSet set = statement.executeQuery();
                            set.first();
                            String result = set.getString(column);
                            entry = result.split(";;");
                            setEntry(key, entry);
                        } catch (SQLException ex) {
                            RalexBot.logSevere("An error occured", ex);
                        }
                    }
                    break;
                }
            }
            return entry;
        }

        public boolean setEntry(String key, String[] newEntry) {
            synchronized (factoids) {
                factoids.put(key, newEntry);
            }
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
    }
}
