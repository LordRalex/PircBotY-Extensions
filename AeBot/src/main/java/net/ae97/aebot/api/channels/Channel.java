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
package net.ae97.aebot.api.channels;

import net.ae97.aebot.api.Utilities;
import net.ae97.aebot.api.sender.Sender;
import net.ae97.aebot.permissions.Permissible;
import net.ae97.aebot.permissions.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.pircbotx.User;

/**
 *
 * @author Joshua
 */
public class Channel extends Utilities implements Sender, Permissible {

    private final org.pircbotx.Channel pircbotxChannel;
    protected final Map<String, Set<Permission>> permMap = new HashMap<>();
    protected final static Map<org.pircbotx.Channel, net.ae97.aebot.api.channels.Channel> existingChannels = new ConcurrentHashMap<>();

    protected Channel(String name) {
        pircbotxChannel = bot.getChannel(name);
    }

    protected Channel(org.pircbotx.Channel chan) {
        pircbotxChannel = chan;
    }

    public static Channel getChannel(org.pircbotx.Channel temp) {
        net.ae97.aebot.api.channels.Channel chan = existingChannels.get(temp);
        if (chan == null) {
            chan = new Channel(temp);
            existingChannels.put(temp, chan);
        }
        return chan;
    }

    public static Channel getChannel(String name) {
        org.pircbotx.Channel temp = bot.getChannel(name);
        return getChannel(temp);
    }

    @Override
    public void sendMessage(String message) {
        bot.sendMessage(pircbotxChannel, message);
    }

    @Override
    public void sendNotice(String message) {
        bot.sendNotice(pircbotxChannel, message);
    }

    @Override
    public void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public void sendNotice(String[] messages) {
        for (String message : messages) {
            sendNotice(message);
        }
    }

    public boolean isSecret() {
        return pircbotxChannel.isSecret();
    }

    public String[] getOPs() {
        User[] users = pircbotxChannel.getOps().toArray(new User[0]);
        String[] names = new String[users.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = users[i].getNick();
        }
        return names;
    }

    public String[] getVoiced() {
        User[] users = pircbotxChannel.getVoices().toArray(new User[0]);
        String[] names = new String[users.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = users[i].getNick();
        }
        return names;
    }

    public boolean hasOP(String name) {
        return pircbotxChannel.isOp(bot.getUser(name));
    }

    public boolean hasVoice(String name) {
        return pircbotxChannel.hasVoice(bot.getUser(name));
    }

    public String getName() {
        return pircbotxChannel.getName();
    }

    public List<String> getUsers() {
        Set<User> users = pircbotxChannel.getUsers();
        List<String> names = new ArrayList<>();
        for (User user : users) {
            names.add(user.getNick());
        }
        return names;
    }

    @Override
    public boolean hasPermission(String channel, String perm) {
        Set<Permission> set = permMap.get(channel == null ? null : channel.toLowerCase());
        if (set != null) {
            for (Permission p : set.toArray(new Permission[set.size()])) {
                if (p.getName().equalsIgnoreCase(perm)) {
                    return true;
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

    public void setMode(String mode) {
        bot.setMode(pircbotxChannel, mode);
    }

    public void setMode(char mode, boolean newState) {
        String newMode = (newState ? "+" : "-") + mode;
        setMode(newMode);
    }
}
