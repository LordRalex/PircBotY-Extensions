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
package org.hoenn.pokebot.api.users;

import org.hoenn.pokebot.api.Utilities;
import org.hoenn.pokebot.api.sender.Sender;
import org.hoenn.pokebot.permissions.Permissible;
import org.hoenn.pokebot.permissions.Permission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hoenn.pokebot.api.channels.Channel;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.WhoisEvent;

/**
 *
 * @author Joshua
 */
public class User extends Utilities implements Sender, Permissible {

    protected final org.pircbotx.User pircbotxUser;
    protected final Map<String, Set<Permission>> permMap = new HashMap<>();
    protected final static Map<org.pircbotx.User, org.hoenn.pokebot.api.users.User> existingUsers = new ConcurrentHashMap<>();

    protected User(String nick) {
        pircbotxUser = bot.getUser(nick);
    }

    protected User(org.pircbotx.User u) {
        pircbotxUser = u;
    }

    protected User(org.pircbotx.UserSnapshot snap) {
        pircbotxUser = snap;
    }

    public static User getUser(String username) {
        org.pircbotx.User temp = bot.getUser(username);
        return getUser(temp);
    }

    public static User getUser(org.pircbotx.User pbUser) {
        org.hoenn.pokebot.api.users.User user = existingUsers.get(pbUser);
        if (user == null) {
            user = new User(pbUser);
            existingUsers.put(pbUser, user);
        }
        return user;
    }

    @Override
    public void sendMessage(String message) {
        pircbotxUser.sendMessage(message);
    }

    @Override
    public void sendNotice(String message) {
        bot.sendNotice(pircbotxUser, message);
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

    public boolean hasVoice(String channel) {
        return hasVoice(bot.getChannel(channel));
    }

    public boolean hasVoice(org.hoenn.pokebot.api.channels.Channel channel) {
        return hasVoice(bot.getChannel(channel.getName()));
    }

    public boolean hasVoice(org.pircbotx.Channel channel) {
        return pircbotxUser.getChannelsVoiceIn().contains(channel);
    }

    public String getLoginName() {
        return pircbotxUser.getLogin();
    }

    public boolean hasOP(String channel) {
        return hasOP(bot.getChannel(channel));
    }

    public boolean hasOP(org.hoenn.pokebot.api.channels.Channel channel) {
        return hasOP(bot.getChannel(channel.getName()));
    }

    public boolean hasOP(org.pircbotx.Channel channel) {
        return pircbotxUser.getChannelsOpIn().contains(channel);
    }

    public String isVerified() {
        String name = null;
        try (WaitForQueue queue = new WaitForQueue(pircbotxUser.getBot())) {
            WhoisEvent evt;
            try {
                pircbotxUser.getBot().sendRawLineNow("whois " + pircbotxUser.getNick());
                while (true) {
                    evt = queue.waitFor(WhoisEvent.class);
                    if (evt.getNick()
                            .equals(this.pircbotxUser.getNick())) {
                        name = evt.getRegisteredAs();
                        break;
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(User.class
                        .getName()).log(Level.SEVERE, null, ex);
                name = null;
            }
            if (name != null && name.isEmpty()) {
                name = null;
            }
        }
        return name;
    }

    public String[] getChannels() {
        org.pircbotx.Channel[] chanArray = pircbotxUser.getChannels().toArray(new org.pircbotx.Channel[0]);
        String[] channelList = new String[chanArray.length];
        for (int i = 0; i < channelList.length; i++) {
            channelList[i] = chanArray[i].getName();
        }
        return channelList;
    }

    public String getIP() {
        return pircbotxUser.getHostmask();
    }

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

    public boolean hasPermission(org.pircbotx.Channel channel, String perm) {
        if (channel == null) {
            return hasPermission((String) null, perm);
        }
        return hasPermission(channel.getName().toLowerCase(), perm);
    }

    public boolean hasPermission(org.hoenn.pokebot.api.channels.Channel channel, String perm) {
        if (channel == null) {
            return hasPermission((String) null, perm);
        }
        return hasPermission(channel.getName().toLowerCase(), perm);
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

    public String getRealName() {
        return pircbotxUser.getRealName();
    }

    public void op(String channel) {
        op(Channel.getChannel(channel));
    }

    public void op(org.hoenn.pokebot.api.channels.Channel channel) {
        BotUser.getBotUser().op(channel, this);
    }

    public void deop(String channel) {
        deop(Channel.getChannel(channel));
    }

    public void deop(org.hoenn.pokebot.api.channels.Channel channel) {
        BotUser.getBotUser().deop(channel, this);
    }

    public void voice(String channel) {
        voice(Channel.getChannel(channel));
    }

    public void voice(org.hoenn.pokebot.api.channels.Channel channel) {
        BotUser.getBotUser().voice(channel, this);
    }

    public void devoice(String channel) {
        devoice(Channel.getChannel(channel));
    }

    public void devoice(org.hoenn.pokebot.api.channels.Channel channel) {
        BotUser.getBotUser().devoice(channel, this);
    }

    public void kick(String channel) {
        kick(Channel.getChannel(channel));
    }

    public void kick(org.hoenn.pokebot.api.channels.Channel channel) {
        BotUser.getBotUser().kick(channel, this);
    }

    public void ban(String channel) {
        ban(Channel.getChannel(channel));
    }

    public void ban(org.hoenn.pokebot.api.channels.Channel channel) {
        BotUser.getBotUser().ban(channel, this);
    }
}
