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
