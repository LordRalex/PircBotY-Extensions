package com.lordralex.ralexbot.settings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class SettingsMap<T extends String, Object> extends LinkedHashMap<String, Object> {

    public SettingsMap(Map<String, Object> existing) {
        Set<String> keys = existing.keySet();
        for (String key : keys) {
            put(key, existing.get(key));
        }
    }

    public Object get(T key) {
        return get(key, this);
    }

    private Object get(String key, Map<String, Object> map) {
        if (!key.contains(".")) {
            return map.get(key);
        }
        Object obj = map.get(key);
        if (obj == null || !(obj instanceof Map)) {
            return obj;
        }
        String newKey = key.split(".", 2)[1];
        return get(newKey, (Map<String, Object>) obj);
    }
}
