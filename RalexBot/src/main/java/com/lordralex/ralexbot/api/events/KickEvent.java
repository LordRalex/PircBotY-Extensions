/*
 * Copyright (C) 2013 Laptop
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

import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.users.User;

/**
 * @version 1.0
 * @author Laptop
 */
public class KickEvent extends Event {

    private final User reciever;
    private final User sender;
    private final Channel channel;
    private final String message;

    public KickEvent(org.pircbotx.hooks.events.KickEvent event) {
        reciever = new User(event.getRecipient().getNick());
        sender = new User(event.getSource().getNick());
        channel = new Channel(event.getChannel().getName());
        message = event.getReason();
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return reciever;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }
}
