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
import net.ae97.pircboty.PircBotY;
import net.ae97.pircboty.hooks.WaitForQueue;
import net.ae97.pircboty.hooks.events.WhoisEvent;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.permissions.Permission;

/**
 * @author Lord_Ralex
 */
public class PokeBotUser extends User {

    private final net.ae97.pircboty.User PircBotYUser;
    private final PircBotY bot;
    private String verifiedName = null;
    private final Map<String, Set<Permission>> permMap = new HashMap<>();
    private final static Map<net.ae97.pircboty.User, org.hoenn.pokebot.api.users.User> existingUsers = new ConcurrentHashMap<>();

    public PokeBotUser(PircBotY b, String name) {
        this(b, b.getUserChannelDao().getUser(name));
    }

    public PokeBotUser(PircBotY b, net.ae97.pircboty.User u) {
        bot = b;
        PircBotYUser = u;
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            bot.sendIRC().message(PircBotYUser.getNick(), message);
        }
    }

    @Override
    public void sendNotice(String... messages) {
        for (String message : messages) {
            bot.sendIRC().notice(PircBotYUser.getNick(), message);
        }
    }

    @Override
    public String getNickservName() {
        if (verifiedName == null) {
            try (WaitForQueue queue = new WaitForQueue(PircBotYUser.getBot())) {
                WhoisEvent<?> evt;
                try {
                    PircBotYUser.getBot().sendRaw().rawLine("whois " + PircBotYUser.getNick());
                    while (true) {
                        evt = queue.waitFor(WhoisEvent.class);
                        if (evt.getNick().equals(PircBotYUser.getNick())) {
                            verifiedName = evt.getRegisteredAs();
                            break;
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
        Set<net.ae97.pircboty.Channel> chans = PircBotYUser.getChannels();
        String[] channelList = new String[chans.size()];
        int i = 0;
        for (net.ae97.pircboty.Channel ch : chans) {
            channelList[i] = ch.getName();
            i++;
        }
        return channelList;
    }

    @Override
    public String getHost() {
        return PircBotYUser.getHostmask();
    }

    @Override
    public String getNick() {
        return PircBotYUser.getNick();
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
        return PircBotYUser.getRealName();
    }

}
