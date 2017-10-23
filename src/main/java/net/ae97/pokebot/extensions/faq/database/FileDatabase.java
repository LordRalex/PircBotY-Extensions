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
package net.ae97.pokebot.extensions.faq.database;

import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import net.ae97.pokebot.PokeBot;

/**
 *
 * @author Lord_Ralex
 */
public class FileDatabase extends Database {

    private final File loadPath;
    private final File savePath;
    private final int updateTime;
    private final Timer timer;
    private final TimerTask task;
    private final Map<String, String[]> indexMapping = new HashMap<>();

    public FileDatabase(String n, Map<String, String> params) {
        super(n, params);
        loadPath = new File(params.get("load"));
        savePath = new File(params.get("save"));
        updateTime = Integer.parseInt(params.get("update"));
        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                load();
            }

        };
        if (updateTime > 0 && !loadPath.getPath().equals(savePath.getPath())) {
            timer.scheduleAtFixedRate(task, updateTime, updateTime);
        }
    }

    @Override
    public void load() {
        PokeBot.getLogger().info("Updating " + getName());
        if (!loadPath.getPath().equals(savePath.getPath())) {
            try {
                Files.move(savePath, new File(savePath.getPath() + ".backup"));
            } catch (IOException ex) {
                PokeBot.getLogger().log(Level.SEVERE, "Error on updating file database '" + getName() + "'", ex);
            }
            try (InputStream in = loadPath.toURI().toURL().openStream()) {
                try (OutputStream writer = new FileOutputStream(savePath)) {
                    byte[] copy = new byte[1024];
                    int read;
                    while ((read = in.read(copy)) != -1) {
                        writer.write(copy, 0, read);
                    }
                }
            } catch (IOException e) {
                PokeBot.getLogger().log(Level.SEVERE, "Error on updating file database '" + getName() + "'", e);
                try {
                    Files.move(new File(savePath.getPath() + ".backup"), savePath);
                } catch (IOException ex) {
                    PokeBot.getLogger().log(Level.SEVERE, "Error on updating file database '" + getName() + "'", ex);
                }
                return;
            }
        }
        try {
            updateIndex(savePath);
        } catch (IOException ex) {
            PokeBot.getLogger().log(Level.SEVERE, "Error on updating file database '" + getName() + "'", ex);
            try {
                updateIndex(new File(savePath.getPath() + ".backup"));
            } catch (IOException e) {
                PokeBot.getLogger().log(Level.SEVERE, "Error on restoring from backup '" + getName() + "'", e);
            }
        }

    }

    @Override
    public String[] getEntry(String key) {
        synchronized (indexMapping) {
            return indexMapping.get(key.toLowerCase());
        }
    }

    private void updateIndex(File source) throws IOException {
        synchronized (indexMapping) {
            try (BufferedReader reader = new BufferedReader(new FileReader(source))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String name = line.split("\\|")[0];
                    String content = line.split("\\|", 2)[1];
                    indexMapping.put(name.toLowerCase(), content.split(";;"));
                }
            } catch (IndexOutOfBoundsException ex) {
                throw new IOException(ex);
            }
        }
    }
}
