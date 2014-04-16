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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.hoenn.pokebot.PokeBot;

/**
 * @author Lord_Ralex
 */
public class FileDatabase extends Database {

    private final String location;
    private final ConcurrentHashMap<String, String[]> factoids = new ConcurrentHashMap<>();

    public FileDatabase(String n, String path) {
        super(n);
        location = path;
    }

    @Override
    public void load() {
        BufferedReader filereader = null;
        try {
            filereader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(location)), Charset.forName("UTF-8")));
            String line;
            while ((line = filereader.readLine()) != null) {
                if (line.contains("|")) {
                    String key = line.split("\\|")[0];
                    String value = line.split("\\|", 2)[1];
                    factoids.put(key.toLowerCase(), value.split(";;"));
                }
            }
        } catch (IOException ex) {
            PokeBot.getLogger().log(Level.SEVERE, "There was an error", ex);
            PokeBot.getLogger().log(Level.INFO, "Location: " + location);
        } finally {
            try {
                if (filereader != null) {
                    filereader.close();
                }
            } catch (IOException ex) {
                PokeBot.getLogger().log(Level.SEVERE, "There was an error", ex);
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
                    PokeBot.getLogger().log(Level.INFO, "  Inserting " + line);
                    factoids.put(key.toLowerCase(), value.split(";;"));
                }
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            PokeBot.getLogger().log(Level.SEVERE, "There was an error", ex);
            PokeBot.getLogger().log(Level.INFO, "Location: " + location + "-override");
        } finally {
            try {
                if (filereader != null) {
                    filereader.close();
                }
            } catch (IOException ex) {
                PokeBot.getLogger().log(Level.SEVERE, "There was an error", ex);
            }
        }

    }

    public String getFile() {
        return location;
    }

    public void save() throws IOException {
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
    }

    @Override
    public String[] getEntry(String key) {
        String[] entry;
        key = key.toLowerCase().trim();
        synchronized (factoids) {
            entry = factoids.get(key);
        }
        return entry;
    }
}
