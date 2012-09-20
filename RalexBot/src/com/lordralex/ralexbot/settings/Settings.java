package com.lordralex.ralexbot.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private static final Map<String, Object> settings = new ConcurrentHashMap<>();

    public static void loadSettings() {
        File settingsFile = new File("settings", "config.yml");
        Yaml yml = new Yaml(new SafeConstructor());
        Iterable<Object> it = null;
        try {
            it = yml.loadAll(new FileInputStream(settingsFile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Object in : it) {
            Set set = ((LinkedHashMap) in).keySet();
            //currently does not do nested vars, only outer, have to loop for LinkedHashMaps for that
            for (Object key : set) {
                System.out.println("Key is " + ((String) key));
                System.out.println("Value was " + ((LinkedHashMap) in).get(key));
                if (((LinkedHashMap) in).get(key) != null) {
                    settings.put((String) key, ((LinkedHashMap) in).get(key));
                }
            }
        }
    }

    public static String getString(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        String value = null;
        Object val = settings.get(key);
        if (val instanceof String) {
            value = (String) val;
        }
        return value;
    }

    public static int getInt(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        Integer value = 0;
        Object val = settings.get(key);
        if (val instanceof Integer) {
            value = (Integer) val;
        }
        return value;
    }

    public static List<String> getStringList(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        List<String> value = null;
        Object val = settings.get(key);
        if (val instanceof List) {
            value = (List<String>) val;
        }
        return value;
    }

    private Settings() {
    }
}