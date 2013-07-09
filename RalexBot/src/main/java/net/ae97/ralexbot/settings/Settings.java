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
package net.ae97.ralexbot.settings;

import net.ae97.ralexbot.RalexBot;
import net.ae97.ralexbot.data.DataStorage;
import net.ae97.ralexbot.data.DataType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class Settings implements DataStorage<String> {

    private static final Map<File, SettingsMap<String, Object>> settings = new ConcurrentHashMap<>();
    private final File name;
    private static final Settings global;

    static {
        global = loadGlobalSettings();
    }

    public Settings(File aFileToLoad, boolean forceLoad) {
        name = aFileToLoad;
        load();
    }

    @Override
    public void load() {
        Map<String, Object> local = new HashMap<>();
        Yaml yml = new Yaml(new SafeConstructor());
        Iterable<Object> it = null;
        try {
            it = yml.loadAll(new FileInputStream(name));
        } catch (FileNotFoundException ex) {
            RalexBot.logSevere("Cannot find " + name, ex);
        }
        if (it != null) {
            for (Object in : it) {
                Set set = ((LinkedHashMap) in).keySet();
                //currently does not do nested vars, only outer, have to loop for LinkedHashMaps for that
                for (Object key : set) {
                    if (((LinkedHashMap) in).get(key) != null) {
                        local.put((String) key, ((LinkedHashMap) in).get(key));
                    }
                }
            }
        }
        settings.put(name, new SettingsMap<>(local));
    }

    public void save() {
    }

    public Settings(File aFileToLoad) {
        this(aFileToLoad, false);
    }

    public static Settings loadGlobalSettings() {
        return (global == null ? new Settings(new File("settings", "config.yml")) : global);
    }

    public static Settings getGlobalSettings() {
        return (global == null ? loadGlobalSettings() : global);
    }

    @Override
    public String getString(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        String value = null;
        Object val = settings.get(name).get(key);
        if (val != null && val instanceof String) {
            value = (String) val;
        }
        return value;
    }

    @Override
    public int getInt(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        Integer value = 0;
        Object val = settings.get(name).get(key);
        if (val != null && val instanceof Integer) {
            value = (Integer) val;
        }
        return value;
    }

    @Override
    public List<String> getStringList(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        List<String> value = null;
        Object val = settings.get(name).get(key);
        if (val != null && val instanceof List) {
            value = (List<String>) val;
        } else if (val != null && val instanceof String[]) {
            value = Arrays.asList((String[]) val);
        } else if (value == null) {
            value = new ArrayList<>();
        }
        return value;
    }

    /**
     * Sets a new value for a key and saves the file.
     *
     * @param key Key
     * @param newValue The new value, this can be lists
     */
    public void set(String key, Object newValue) throws IOException {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        Yaml yml = new Yaml(new SafeConstructor());
        settings.get(name).put(key, newValue);
        FileWriter out = new FileWriter(name);
        yml.dump(settings.get(name), out);
    }

    @Override
    public boolean getBoolean(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        Boolean value = Boolean.FALSE;
        Object val = settings.get(name).get(key);
        if (val != null && val instanceof Boolean) {
            value = (Boolean) val;
        }
        return value;
    }

    @Override
    public DataType getType() {
        return DataType.FLAT;
    }
}
