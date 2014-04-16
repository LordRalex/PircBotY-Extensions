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
package org.hoenn.pokebot.permissions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.hoenn.pokebot.api.events.PermissionEvent;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.configuration.InvalidConfigurationException;
import org.hoenn.pokebot.configuration.file.YamlConfiguration;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class PermissionManager {

    private final YamlConfiguration permFile;
    private final Map<User, Long> cache = new ConcurrentHashMap<>();
    private final int CACHE_TIME = 1000 * 60 * 5;

    public PermissionManager() {
        permFile = new YamlConfiguration();
    }

    public void load() throws IOException {
        if (!new File("permissions.yml").exists()) {
            new File("permissions.yml").createNewFile();
        }
        try {
            permFile.load(new File("permissions.yml"));
        } catch (InvalidConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    public void reload() throws IOException {
        synchronized (cache) {
            cache.clear();
        }
        try {
            permFile.load(new File("permissions.yml"));
        } catch (InvalidConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    public void runPermissionEvent(PermissionEvent event) {
        User user = event.getUser();
        synchronized (cache) {
            if (!event.isForced()) {
                if (cache.containsKey(user) && cache.get(user) != null && cache.get(user) > System.currentTimeMillis() + CACHE_TIME) {
                    return;
                }
            }
            cache.put(user, System.currentTimeMillis() + CACHE_TIME);
        }
        String ver = user.getNickservName();
        if (ver == null || ver.isEmpty()) {
            return;
        }
        Map<String, Set<Permission>> existing = user.getPermissions();
        for (String key : existing.keySet().toArray(new String[0])) {
            for (Permission perm : existing.get(key).toArray(new Permission[0])) {
                user.removePermission(key, perm.getName());
            }
        }
        List<String> list = permFile.getStringList(ver);
        for (String line : list) {
            String chan = line.split("\\|")[0];
            String perm = line.split("\\|")[1];
            if (chan.isEmpty()) {
                chan = null;
            }
            user.addPermission(chan, perm);
        }
    }
}
