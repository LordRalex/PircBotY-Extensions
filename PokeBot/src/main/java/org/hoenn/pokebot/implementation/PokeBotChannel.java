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
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.permissions.Permission;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

/**
 * @author Lord_Ralex
 */
public class PokeBotChannel extends Channel {

    private final org.pircbotx.Channel pircbotxChannel;
    private final org.pircbotx.PircBotX bot;
    protected final Map<String, Set<Permission>> permMap = new HashMap<>();

    public PokeBotChannel(PircBotX aB, org.pircbotx.Channel channel) {
        bot = aB;
        pircbotxChannel = channel;
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            bot.sendMessage(pircbotxChannel, message);
        }
    }

    @Override
    public void sendNotice(String... messages) {
        for (String message : messages) {
            bot.sendNotice(pircbotxChannel, message);
        }
    }

    @Override
    public void sendAction(String... messages) {
        for (String message : messages) {
            sendAction(message);
        }
    }

    @Override
    public boolean isSecret() {
        return pircbotxChannel.isSecret();
    }

    @Override
    public String[] getOps() {
        User[] users = pircbotxChannel.getOps().toArray(new User[0]);
        String[] names = new String[users.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = users[i].getNick();
        }
        return names;
    }

    @Override
    public String[] getVoiced() {
        User[] users = pircbotxChannel.getVoices().toArray(new User[0]);
        String[] names = new String[users.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = users[i].getNick();
        }
        return names;
    }

    @Override
    public boolean hasOp(String name) {
        return pircbotxChannel.isOp(bot.getUser(name));
    }

    @Override
    public boolean hasVoice(String name) {
        return pircbotxChannel.hasVoice(bot.getUser(name));
    }

    @Override
    public String getName() {
        return pircbotxChannel.getName();
    }

    @Override
    public String[] getUserList() {
        Set<User> users = pircbotxChannel.getUsers();
        LinkedList<String> names = new LinkedList<>();
        for (User user : users) {
            names.add(user.getNick());
        }
        return names.toArray(new String[names.size()]);
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

    @Override
    public void setMode(char mode, boolean newState) {
        String newMode = (newState ? "+" : "-") + mode;
        bot.setMode(pircbotxChannel, newMode);
    }

    @Override
    public void kickUser(String name, String reason) {
        if (reason != null) {
            bot.kick(pircbotxChannel, bot.getUser(name), reason);
        } else {
            bot.kick(pircbotxChannel, bot.getUser(name));
        }
    }

    @Override
    public void ban(String mask) {
        bot.ban(pircbotxChannel, mask);
    }

    @Override
    public void opUser(String user) {
        pircbotxChannel.op(bot.getUser(user));
    }

    @Override
    public void deopUser(String user) {
        pircbotxChannel.deOp(bot.getUser(user));
    }

    @Override
    public void voiceUser(String user) {
        pircbotxChannel.voice(bot.getUser(user));
    }

    @Override
    public void devoiceUser(String user) {
        pircbotxChannel.deVoice(bot.getUser(user));
    }
}
