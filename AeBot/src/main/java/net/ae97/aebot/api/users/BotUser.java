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
package net.ae97.aebot.api.users;

import net.ae97.aebot.permissions.Permission;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Joshua
 */
public class BotUser extends User {

    protected BotUser() {
        super(bot.getUserBot());
    }

    public static BotUser getBotUser() {
        return new BotUser();
    }

    public void setNick(String newNick) {
        bot.changeNick(newNick);
    }

    public void kick(String nick, String channel) {
        kick(nick, channel, null);
    }

    public void kick(net.ae97.aebot.api.channels.Channel chan, User user) {
        kick(user.getNick(), chan.getName());
    }

    public void kick(String nick, String channel, String reason) {
        if (bot.getChannel(channel).isOp(bot.getUserBot())) {
            if (reason == null || reason.isEmpty()) {
                bot.kick(bot.getChannel(channel), bot.getUser(nick));
            } else {
                bot.kick(bot.getChannel(channel), bot.getUser(nick), reason);
            }
        } else {
            this.sendMessage("chanserv", "kick " + channel + " " + nick + " " + reason);
        }
    }

    public void joinChannel(String channel) {
        bot.joinChannel(channel);
    }

    public void leaveChannel(String channel) {
        bot.partChannel(bot.getChannel(channel));
    }

    public void sendMessage(String target, String message) {
        bot.sendMessage(target, message);
    }

    public void sendNotice(String target, String message) {
        bot.sendNotice(target, message);
    }

    public void sendAction(String target, String message) {
        bot.sendAction(target, message);
    }

    public void ban(net.ae97.aebot.api.channels.Channel chan, User user) {
        ban(chan.getName(), user.getIP());
    }

    public void ban(String channel, String mask) {
        if (bot.getChannel(channel).isOp(bot.getUserBot())) {
            bot.ban(bot.getChannel(channel), mask);
        } else {
            this.sendMessage("chanserv", "ban " + channel + " " + mask);
        }
    }

    public void unban(String channel, String mask) {
        if (bot.getChannel(channel).isOp(bot.getUserBot())) {
            bot.unBan(bot.getChannel(channel), mask);
        } else {
            this.sendMessage("chanserv", "unban " + channel + " " + mask);
        }
    }

    public void op(String channel, String name) {
        if (bot.getChannel(channel).isOp(bot.getUserBot())) {
            bot.op(bot.getChannel(channel), bot.getUser(name));
        } else {
            this.sendMessage("chanserv", "op " + channel + " " + name);
        }
    }

    public void op(net.ae97.aebot.api.channels.Channel chan, net.ae97.aebot.api.users.User user) {
        op(chan.getName(), user.getNick());
    }

    public void deop(String channel, String name) {
        if (bot.getChannel(channel).isOp(bot.getUserBot())) {
            bot.deOp(bot.getChannel(channel), bot.getUser(name));
        } else {
            this.sendMessage("chanserv", "deop " + channel + " " + name);
        }
    }

    public void deop(net.ae97.aebot.api.channels.Channel chan, net.ae97.aebot.api.users.User user) {
        deop(chan.getName(), user.getNick());
    }

    public void voice(String channel, String name) {
        if (bot.getChannel(channel).isOp(bot.getUserBot())) {
            bot.voice(bot.getChannel(channel), bot.getUser(name));
        } else {
            this.sendMessage("chanserv", "voice " + channel + " " + name);
        }
    }

    public void voice(net.ae97.aebot.api.channels.Channel chan, net.ae97.aebot.api.users.User user) {
        voice(chan.getName(), user.getNick());
    }

    public void devoice(String channel, String name) {
        if (bot.getChannel(channel).isOp(bot.getUserBot())) {
            bot.deVoice(bot.getChannel(channel), bot.getUser(name));
        } else {
            this.sendMessage("chanserv", "devoice " + channel + " " + name);
        }
    }

    public void devoice(net.ae97.aebot.api.channels.Channel chan, net.ae97.aebot.api.users.User user) {
        devoice(chan.getName(), user.getNick());
    }

    @Override
    public boolean hasPermission(String channel, String perm) {
        return true;
    }

    @Override
    public void addPermission(String channel, String perm) {
    }

    @Override
    public void removePermission(String channel, String perm) {
    }

    @Override
    public Map<String, Set<Permission>> getPermissions() {
        return new HashMap<>();
    }
}
