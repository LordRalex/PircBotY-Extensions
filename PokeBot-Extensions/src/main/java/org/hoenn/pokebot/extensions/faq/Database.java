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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.data.DataType;
import org.hoenn.pokebot.mysql.MySQLConnection;

/**
 * @author Lord_Ralex
 */
public class Database {

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

    public void load() {
        switch (storage) {
            case FLAT:
                BufferedReader filereader = null;
                try {
                    filereader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(location)), Charset.forName("UTF-8")));
                    String line;
                    while ((line = filereader.readLine()) != null) {
                        if (line.contains("|")) {
                            String key = line.split("\\|")[0];
                            String value = line.split("\\|", 2)[1];
                            setEntry(key.toLowerCase(), value.split(";;"));
                        }
                    }
                } catch (IOException ex) {
                    PokeBot.log(Level.SEVERE, "There was an error", ex);
                    PokeBot.log(Level.INFO, "Location: " + location);
                } finally {
                    try {
                        if (filereader != null) {
                            filereader.close();
                        }
                    } catch (IOException ex) {
                        PokeBot.log(Level.SEVERE, "There was an error", ex);
                    }
                }
                filereader = null;
                try {
                    filereader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(location + "-override")), Charset.forName("UTF-8")));
                    String line;
                    while ((line = filereader.readLine()) != null) {
                        if (line.contains("|")) {
                            String key = line.split("\\|")[0];
                            String value = line.split("\\|", 2)[1];
                            PokeBot.log(Level.INFO, "  Inserting " + line);
                            setEntry(key.toLowerCase(), value.split(";;"));
                        }
                    }
                } catch (FileNotFoundException ex) {
                } catch (IOException ex) {
                    PokeBot.log(Level.SEVERE, "There was an error", ex);
                    PokeBot.log(Level.INFO, "Location: " + location + "-override");
                } finally {
                    try {
                        if (filereader != null) {
                            filereader.close();
                        }
                    } catch (IOException ex) {
                        PokeBot.log(Level.SEVERE, "There was an error", ex);
                    }
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
                    ResultSet set = null;
                    MySQLConnection conn = null;
                    try {
                        String[] details = location.split("\\|");
                        String host = details[0];
                        String port = details[1];
                        String user = details[2];
                        String pass = details[3];
                        String db = details[4];
                        String table = details[5];
                        String column = details[6];
                        conn = new MySQLConnection(host, Integer.parseInt(port), user, pass, db, table);
                        conn.load();
                        PreparedStatement st = conn.getPreparedStatement("USE ?");
                        st.setString(1, db);
                        st.execute();
                        PreparedStatement statement = conn.getPreparedStatement("SELECT ? FROM ? WHERE name=?;");
                        statement.setString(1, column);
                        statement.setString(2, ((MySQLConnection) conn).getTable());
                        statement.setString(3, name);
                        set = statement.executeQuery();
                        set.first();
                        String result = set.getString(column);
                        entry = result.split(";;");
                        setEntry(key, entry);
                    } catch (SQLException ex) {
                        PokeBot.log(Level.SEVERE, "An error occured", ex);
                    } finally {
                        try {
                            if (set != null) {
                                set.close();
                            }
                        } catch (SQLException ex) {
                            PokeBot.log(Level.SEVERE, "An error occured", ex);
                        }
                        try {
                            if (conn != null) {
                                conn.getConnection().close();
                            }
                        } catch (SQLException ex) {
                            PokeBot.log(Level.SEVERE, "An error occured", ex);
                        }
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

    public void save() throws IOException {
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
