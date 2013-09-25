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
package net.ae97.aebot.permissions;

import net.ae97.aebot.api.events.PermissionEvent;
import net.ae97.aebot.api.users.User;
import net.ae97.aebot.settings.Settings;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class PermissionManager {

    private final Settings permFile;
    private final Map<User, Long> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService srv;

    public PermissionManager() {
        permFile = new Settings(new File("permissions", "permFile.yml"));
        srv = Executors.newSingleThreadScheduledExecutor();
        srv.scheduleAtFixedRate(new CacheRunnable(), 5, 5, TimeUnit.MINUTES);
    }

    public void reloadFile() {
        permFile.load();
    }

    public void runPermissionEvent(PermissionEvent event) {
        User user = event.getUser();
        synchronized (cache) {
            if (!event.isForced()) {
                if (cache.containsKey(user)) {
                    return;
                }
            }
            cache.put(user, System.currentTimeMillis());
        }
        String ver = user.isVerified();
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

    private class CacheRunnable implements Runnable {

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            synchronized (cache) {
                Set<User> toRemove = new HashSet<>();
                for (Entry<User, Long> entry : cache.entrySet()) {
                    if (entry.getValue() + (10 * 60 * 1000) < currentTime) {
                        toRemove.add(entry.getKey());
                    }
                }
                for (User user : toRemove) {
                    cache.remove(user);
                }
            }
        }
    }
}
