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
package org.hoenn.pokebot.extension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;
import net.ae97.pircboty.PrefixLogger;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.configuration.InvalidConfigurationException;
import org.hoenn.pokebot.configuration.file.YamlConfiguration;

/**
 * @author Lord_Ralex
 */
public abstract class Extension {

    private final File dataFolder = new File("config", getName().replace(" ", "_"));
    private final YamlConfiguration configuration = new YamlConfiguration();
    private final Logger logger = new PrefixLogger(getName(), PokeBot.getLogger());

    public final void initialize() throws ExtensionLoadFailedException {
        try {
            configuration.load(new File(dataFolder, "config.yml"));
        } catch (FileNotFoundException e) {
        } catch (IOException | InvalidConfigurationException ex) {
            throw new ExtensionLoadFailedException(ex);
        }
    }

    public void load() throws ExtensionLoadFailedException {
    }

    public void unload() throws ExtensionUnloadFailedException {
    }

    public void reload() throws ExtensionReloadFailedException {
        try {
            reloadConfig();
        } catch (IOException | InvalidConfigurationException ex) {
            throw new ExtensionReloadFailedException(ex);
        }
    }

    public abstract String getName();

    public File getDataFolder() {
        return dataFolder;
    }

    public YamlConfiguration getConfig() {
        return configuration;
    }

    public void saveConfig() throws IOException {
        configuration.save(new File(getDataFolder(), "config.yml"));
    }

    public void reloadConfig() throws IOException, InvalidConfigurationException {
        configuration.load(new File(getDataFolder(), "config.yml"));
    }

    public Logger getLogger() {
        return logger;
    }
}
