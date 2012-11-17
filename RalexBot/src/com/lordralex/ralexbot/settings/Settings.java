package com.lordralex.ralexbot.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class Settings {

    private static final Map<File, Map<String, Object>> settings = new ConcurrentHashMap<>();
    private File name;

    public Settings(File aFileToLoad) {
        name = aFileToLoad;
        if (settings.containsKey(name)) {
            return;
        }
        Map<String, Object> local = new HashMap<>();
        Yaml yml = new Yaml(new SafeConstructor());
        Iterable<Object> it = null;
        try {
            it = yml.loadAll(new FileInputStream(name));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
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
        settings.put(name, local);
    }

    public static Settings loadGlobalSettings() {
        File settingsFile = new File("settings", "config.yml");
        return new Settings(settingsFile);
    }

    public String getString(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        String value = null;
        Object val = settings.get(name).get(key);
        if (val instanceof String) {
            value = (String) val;
        }
        return value;
    }

    public int getInt(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        Integer value = 0;
        Object val = settings.get(name).get(key);
        if (val instanceof Integer) {
            value = (Integer) val;
        }
        return value;
    }

    public List<String> getStringList(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        List<String> value = null;
        Object val = settings.get(name).get(key);
        if (val instanceof List) {
            value = (List<String>) val;
        } else if (val instanceof String[]) {
            value = Arrays.asList((String[]) val);
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
}