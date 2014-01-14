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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class Settings {

    private Map<String, Object> mapping = new HashMap<>();

    public void load(File file) throws IOException {
        Map<String, Object> local = new HashMap<>();
        Yaml yml = new Yaml(new SafeConstructor());
        Iterable<Object> it = null;
        try {
            it = yml.loadAll(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            throw ex;
        }
        if (it != null) {
            for (Object in : it) {
                LinkedHashMap map = (LinkedHashMap) in;
                local.putAll(map);
            }
        }
        mapping = new HashMap<>(local);
    }

    public void load(String line) {
        Map<String, Object> local = new HashMap<>();
        Yaml yml = new Yaml(new SafeConstructor());
        Iterable<Object> it = yml.loadAll(line);
        if (it != null) {
            for (Object in : it) {
                LinkedHashMap map = (LinkedHashMap) in;
                local.putAll(map);
            }
        }
        mapping = new HashMap<>(local);
    }

    public void load(InputStream input) {
        Map<String, Object> local = new HashMap<>();
        Yaml yml = new Yaml(new SafeConstructor());
        Iterable<Object> it = yml.loadAll(input);
        if (it != null) {
            for (Object in : it) {
                LinkedHashMap map = (LinkedHashMap) in;
                local.putAll(map);
            }
        }
        mapping = new HashMap<>(local);
    }

    public Object get(String key) {
        return get(mapping, key);
    }

    private Object get(Map<String, Object> map, String key) {
        if (key == null) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        if (map == null) {
            return null;
        }
        if (key.contains(".")) {
            String part = key.split("\\.")[0];
            String rest = key.split("\\.", 2)[1];
            return get((Map<String, Object>) map.get(part), rest);
        }
        return map.get(key);
    }

    public String getString(String key) {
        String value = null;
        Object val = get(key);
        if (val != null && val instanceof String) {
            value = (String) val;
        }
        return value;
    }

    public int getInt(String key) {
        Integer value = 0;
        Object val = get(key);
        if (val != null && val instanceof Integer) {
            value = (Integer) val;
        }
        return value;
    }

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
    public void set(String key, Object newValue) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        set(mapping, key, newValue);
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

    public boolean getBoolean(String key) {
        Boolean value = Boolean.FALSE;
        Object val = get(key);
        if (val != null && val instanceof Boolean) {
            value = (Boolean) val;
        }
        return value;
    }
}
