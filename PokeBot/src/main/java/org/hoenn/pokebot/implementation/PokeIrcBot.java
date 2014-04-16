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

import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

/**
 *
 * @author Lord_Ralex
 */
public class PokeIrcBot extends PircBotX {

    public PokeIrcBot(Configuration<? extends PircBotX> configuration) {
        super(configuration);
    }

    public User getUser(String name) {
        return getUserChannelDao().getUser(name);
    }

    public Channel getChannel(String name) {
        return getUserChannelDao().getChannel(name);
    }

    public void sendMessage(String target, String... message) {
        for (String m : message) {
            sendIRC().message(target, m);
        }
    }

    public void sendNotice(String target, String... message) {
        for (String m : message) {
            sendIRC().notice(target, m);
        }
    }

    public void sendMessage(User target, String... message) {
        for (String m : message) {
            sendIRC().message(target.getNick(), m);
        }
    }

    public void sendNotice(User target, String... message) {
        for (String m : message) {
            sendIRC().notice(target.getNick(), m);
        }
    }

    public void sendMessage(Channel target, String... message) {
        for (String m : message) {
            sendIRC().message(target.getName(), m);
        }
    }

    public void sendNotice(Channel target, String... message) {
        for (String m : message) {
            sendIRC().notice(target.getName(), m);
        }
    }

}
