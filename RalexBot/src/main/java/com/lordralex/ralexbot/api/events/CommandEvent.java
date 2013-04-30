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
package com.lordralex.ralexbot.api.events;

import com.lordralex.ralexbot.EventHandler;
import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.exceptions.NickNotOnlineException;
import com.lordralex.ralexbot.api.users.User;

public final class CommandEvent extends Event {

    private final String command;
    private final User sender;
    private final Channel channel;
    private final String[] args;

    public CommandEvent(org.pircbotx.hooks.events.MessageEvent event) {
        String[] temp = event.getMessage().split(" ");
        String commandTemp = temp[0].toLowerCase();
        for (String cmd : EventHandler.getCommandPrefixes()) {
            if (commandTemp.startsWith(cmd)) {
                commandTemp = commandTemp.substring(cmd.length());
                break;
            }
        }
        command = commandTemp;
        sender = new User(event.getUser());
        channel = new Channel(event.getChannel());
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public CommandEvent(org.pircbotx.hooks.events.PrivateMessageEvent event) {
        String[] temp = event.getMessage().split(" ");
        command = temp[0].substring(1).toLowerCase();
        sender = new User(event.getUser().getNick());
        channel = null;
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public CommandEvent(org.pircbotx.hooks.events.NoticeEvent event) throws NickNotOnlineException {
        String[] temp = event.getMessage().split(" ");
        command = temp[0].substring(1).toLowerCase();
        sender = new User(event.getUser().getNick());
        channel = null;
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public String getCommand() {
        return command;
    }

    public User getSender() {
        return sender;
    }

    public Channel getChannel() {
        return channel;
    }

    public String[] getArgs() {
        return args;
    }
}
