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
package com.lordralex.ralexbot.api.users;

import com.lordralex.ralexbot.api.Utilities;
import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.sender.Sender;
import com.lordralex.ralexbot.permissions.PermissionGroup;
import com.lordralex.ralexbot.settings.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Joshua
 */
public class User extends Utilities implements Sender {

    protected final org.pircbotx.User pircbotxUser;
    protected final List<String> perms = new ArrayList<>();

    protected User(String nick) {
        pircbotxUser = bot.getUser(nick);
        List<String> permsToGet = new ArrayList<>();
        Settings userGroups = new Settings(new File("permissions", "users.yml"));
        List<String> groups = userGroups.getStringList(nick.toLowerCase());
        if (groups != null) {
            for (String groupName : groups) {
                PermissionGroup group = new PermissionGroup(groupName);
                permsToGet.addAll(group.getPerms());
            }
        }
        perms.addAll(permsToGet);
    }

    public static User getUser(String nick) {
        return new User(nick);
    }

    @Override
    public void sendMessage(String message) {
        bot.sendMessage(pircbotxUser, message);
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
        return bot.getChannel(channel).hasVoice(pircbotxUser);
    }

    public boolean hasOP(String channel) {
        return bot.getChannel(channel).isOp(pircbotxUser);
    }

    public boolean isVerified() {
        return pircbotxUser.isIdentified();
    }

    public String[] getChannels() {
        Channel[] chanArray = pircbotxUser.getChannels().toArray(new Channel[0]);
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

    public void quiet(String channel) {
        bot.sendMessage("chanserv", "quiet " + channel + " *!*" + pircbotxUser.getLogin() + "@" + pircbotxUser.getHostmask());
    }

    public void quiet(Channel channel) {
        quiet(channel.getName());
    }

    public void unquiet(String channel) {
        bot.sendMessage("chanserv", "unquiet " + channel + " *!*" + pircbotxUser.getLogin() + "@" + pircbotxUser.getHostmask());
    }

    public String getQuietLine() {
        return "*!*" + pircbotxUser.getLogin() + "@" + pircbotxUser.getHostmask();
    }

    public boolean hasPermission(String permission) {
        return perms.contains(permission);
    }

    public void addPerm(String perm) {
        perms.add(perm);
    }
}
