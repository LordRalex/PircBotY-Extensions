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
package org.hoenn.pokebot.settings;

import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.data.DataStorage;
import org.hoenn.pokebot.data.DataType;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class Settings implements DataStorage<String> {

    private static final Map<File, Map<String, Object>> settings = new ConcurrentHashMap<>();
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
            PokeBot.log(Level.SEVERE, "Cannot find " + name, ex);
        }
        if (it != null) {
            for (Object in : it) {
                LinkedHashMap map = (LinkedHashMap) in;
                local.putAll(map);
            }
        }
        settings.put(name, new HashMap<>(local));
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

    public Object get(String key) {
        return get(settings.get(name), key);
    }

    private Object get(Map<String, Object> map, String key) {
        if (key == null) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        if (key.contains(".")) {
            String part = key.split("\\.")[0];
            String rest = key.split("\\.", 2)[1];
            return get((Map<String, Object>) map.get(part), rest);
        }
        return map.get(key);
    }

    @Override
    public String getString(String key) {
        String value = null;
        Object val = get(key);
        if (val != null && val instanceof String) {
            value = (String) val;
        }
        return value;
    }

    @Override
    public int getInt(String key) {
        Integer value = 0;
        Object val = get(key);
        if (val != null && val instanceof Integer) {
            value = (Integer) val;
        }
        return value;
    }

    @Override
    public List<String> getStringList(String key) {
        List<String> value = null;
        Object val = get(key);
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
        set(settings.get(name), key, newValue);
        FileWriter out = new FileWriter(name);
        yml.dump(settings.get(name), out);
    }

    private void set(Map<String, Object> map, String key, Object newValue) {
        if (key == null) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        if (key.contains(".")) {
            String part = key.split("\\.")[0];
            String rest = key.split("\\.", 2)[1];
            set((Map<String, Object>) map.get(part), rest, newValue);
        }
        map.put(key, newValue);
    }

    @Override
    public boolean getBoolean(String key) {
        Boolean value = Boolean.FALSE;
        Object val = get(key);
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
