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
package com.lordralex.ralexbot.api.channels;

import com.lordralex.ralexbot.api.Utilities;
import com.lordralex.ralexbot.api.sender.Sender;
import org.pircbotx.User;

/**
 *
 * @author Joshua
 */
public class Channel extends Utilities implements Sender {

    private final org.pircbotx.Channel pircbotxChannel;

    private Channel(String name) {
        pircbotxChannel = bot.getChannel(name);
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

    public static Channel getChannel(String channel) {
        return new Channel(channel);
    }
}
