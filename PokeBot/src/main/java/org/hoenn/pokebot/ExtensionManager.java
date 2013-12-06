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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.hoenn.pokebot.api.CommandExecutor;
import org.hoenn.pokebot.api.Listener;
import org.hoenn.pokebot.extension.Extension;
import org.hoenn.pokebot.loader.ExtensionPluginLoader;
import org.pircbotx.PircBotX;

/**
 * @author Lord_Ralex
 */
public class ExtensionManager {

    private final PircBotX driver;
    private final ExtensionPluginLoader pluginLoader;
    private final PokeBot pokebot;
    private final Set<Extension> loadedExtensions = new HashSet<>();

    public ExtensionManager(PokeBot instance, PircBotX d) {
        pokebot = instance;
        driver = d;
        pluginLoader = new ExtensionPluginLoader();
    }

    public void load() {
        File extensionFolder = new File("extensions");
        File temp = new File("tempDir");
        if (temp.listFiles() != null) {
            for (File file : temp.listFiles()) {
                if (file != null) {
                    file.delete();
                }
            }
        }
        temp.delete();
        extensionFolder.mkdirs();

        for (File file : extensionFolder.listFiles()) {
            try {
                if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                    Extension extension = pluginLoader.loadExtension(file);
                    extension.load();
                    loadedExtensions.add(extension);
                } else if (file.getName().endsWith(".zip")) {
                    ZipFile zipFile = null;
                    try {
                        zipFile = new ZipFile(file);
                        Enumeration entries = zipFile.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = (ZipEntry) entries.nextElement();
                            if (entry.getName().contains("/")) {
                                (new File(temp, entry.getName().split("/")[0])).mkdir();
                            }
                            if (entry.isDirectory()) {
                                new File(temp, entry.getName()).mkdirs();
                                continue;
                            }
                            PokeBot.copyInputStream(zipFile.getInputStream(entry),
                                    new BufferedOutputStream(new FileOutputStream(temp + File.separator + entry.getName())));
                        }
                    } catch (IOException ex) {
                        PokeBot.log(Level.SEVERE, "An error occured", ex);
                    } finally {
                        if (zipFile != null) {
                            try {
                                zipFile.close();
                            } catch (IOException ex) {
                                PokeBot.log(Level.SEVERE, "An error occured", ex);
                            }
                        }
                    }
                } else if (file.getName().endsWith(".jar")) {
                    Extension extension = pluginLoader.loadExtension(file);
                    extension.load();
                    loadedExtensions.add(extension);
                }
            } catch (Exception e) {
                PokeBot.log(Level.SEVERE, "Error on loading extension: " + file.getName(), e);
            }
        }
        if (temp.listFiles() != null) {
            for (File file : temp.listFiles()) {
                try {
                    if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                        Extension extension = pluginLoader.loadExtension(file);
                        extension.load();
                        loadedExtensions.add(extension);
                    } else if (file.getName().endsWith(".jar")) {
                        Extension extension = pluginLoader.loadExtension(file);
                        extension.load();
                        loadedExtensions.add(extension);
                    }
                } catch (Exception e) {
                    PokeBot.log(Level.SEVERE, "Error on loading extension: " + file.getName(), e);
                }
            }
        }
        String names = "";
        for (Extension extension : loadedExtensions) {
            names += extension.getClass().getName() + "|";
        }
        if (names.endsWith("|")) {
            names = names.substring(0, names.length() - 2);
        }
        names = names.replace("|", ", ");
        PokeBot.log("Loaded extensions: " + names);
    }

    public void unload() {
        for (Extension extension : loadedExtensions) {
            extension.unload();
        }
        loadedExtensions.clear();
        pokebot.getEventHandler().unload();
    }

    public void addListener(Listener list) {
        pokebot.getEventHandler().registerListener(list);
    }

    public void addCommandExecutor(CommandExecutor executor) {
        pokebot.getEventHandler().registerCommandExecutor(executor);
    }
}
