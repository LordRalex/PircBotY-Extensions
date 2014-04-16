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
package org.hoenn.pokebot.api.events;

import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.recipients.MessageRecipient;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.eventhandler.EventHandler;

public class CommandEvent implements UserEvent, ChannelEvent, CancellableEvent, ReplyableEvent {

    private final String command;
    private final User sender;
    private final Channel channel;
    private final String[] args;
    private final long timestamp = System.currentTimeMillis();
    private boolean isCancelled = false;

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
        channel = PokeBot.getChannel(event.getChannel().getName());
        sender = PokeBot.getUser(event.getUser().getNick());
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public CommandEvent(org.pircbotx.hooks.events.PrivateMessageEvent event) {
        String[] temp = event.getMessage().split(" ");
        command = temp[0].substring(1).toLowerCase();
        sender = PokeBot.getUser(event.getUser().getNick());
        channel = null;
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public CommandEvent(org.pircbotx.hooks.events.NoticeEvent event) {
        String[] temp = event.getMessage().split(" ");
        command = temp[0].substring(1).toLowerCase();
        sender = PokeBot.getUser(event.getUser().getNick());
        channel = null;
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public User getUser() {
        return sender;
    }

    @Override
    public void setCancelled(boolean state) {
        isCancelled = state;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void reply(String... messages) {
        MessageRecipient rec = channel == null ? sender : channel;
        if (rec == null) {
            return;
        }
        rec.sendMessage(messages);
    }
}
