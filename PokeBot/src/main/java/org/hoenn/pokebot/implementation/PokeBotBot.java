/*
 * Copyright (C) 2014 Lord_Ralex
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
package org.hoenn.pokebot.implementation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hoenn.pokebot.api.users.Bot;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.permissions.Permission;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.WhoisEvent;

/**
 *
 * @author Lord_Ralex
 */
public class PokeBotBot extends Bot {

    private final org.pircbotx.User pircbotxUser;
    private final PircBotX bot;
    private String verifiedName = null;
    private final Map<String, Set<Permission>> permMap = new HashMap<>();
    private final static Map<org.pircbotx.User, org.hoenn.pokebot.api.users.User> existingUsers = new ConcurrentHashMap<>();

    public PokeBotBot(PircBotX b) {
        bot = b;
        pircbotxUser = bot.getUserBot();
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            pircbotxUser.send().message(message);
        }
    }

    @Override
    public void sendNotice(String... messages) {
        for (String message : messages) {
            pircbotxUser.send().notice(message);
        }
    }

    @Override
    public String getNickservName() {
        if (verifiedName == null) {
            try (WaitForQueue queue = new WaitForQueue(pircbotxUser.getBot())) {
                WhoisEvent<?> evt;
                try {
                    boolean done = false;
                    while (!done) {
                        evt = queue.waitFor(WhoisEvent.class);
                        if (evt.getNick().equals(this.pircbotxUser.getNick())) {
                            verifiedName = evt.getRegisteredAs();
                            done = true;
                        }
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(User.class
                            .getName()).log(Level.SEVERE, null, ex);
                    verifiedName = null;
                }
                if (verifiedName != null && verifiedName.isEmpty()) {
                    verifiedName = null;
                }
            }
        }
        return verifiedName;
    }

    @Override
    public String[] getChannels() {
        Set<org.pircbotx.Channel> chans = pircbotxUser.getChannels();
        String[] channelList = new String[chans.size()];
        int i = 0;
        for (org.pircbotx.Channel ch : chans) {
            channelList[i] = ch.getName();
            i++;
        }
        return channelList;
    }

    @Override
    public String getHost() {
        return pircbotxUser.getHostmask();
    }

    @Override
    public String getNick() {
        return pircbotxUser.getNick();
    }

    @Override
    public boolean hasPermission(String channel, String perm) {
        Set<Permission> set = permMap.get(channel == null || channel.isEmpty() ? null : channel.toLowerCase());
        if (set != null) {
            for (Permission p : set.toArray(new Permission[set.size()])) {
                if (p.getName().equalsIgnoreCase(perm)) {
                    return true;
                }
            }
        }
        if (channel == null) {
            return false;
        } else {
            set = permMap.get(null);
            if (set != null) {
                for (Permission p : set.toArray(new Permission[set.size()])) {
                    if (p.getName().equalsIgnoreCase(perm)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void addPermission(String channel, String perm) {
        removePermission(channel, perm);
        Set<Permission> set = permMap.get(channel == null ? null : channel.toLowerCase());
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(new Permission(perm));
        permMap.put(channel == null ? null : channel.toLowerCase(), set);
    }

    @Override
    public void removePermission(String channel, String perm) {
        Set<Permission> set = permMap.get(channel == null ? null : channel.toLowerCase());
        if (set == null) {
            return;
        }
        for (Permission p : set.toArray(new Permission[set.size()])) {
            if (p.getName().equalsIgnoreCase(perm)) {
                set.remove(p);
            }
        }
    }

    @Override
    public Map<String, Set<Permission>> getPermissions() {
        return permMap;
    }

    @Override
    public void setMode(char mode, boolean status) {

    }

    @Override
    public String getName() {
        return pircbotxUser.getRealName();
    }

    @Override
    public void changeNickname(String newName) {
        bot.sendIRC().changeNick(newName);
    }
}
