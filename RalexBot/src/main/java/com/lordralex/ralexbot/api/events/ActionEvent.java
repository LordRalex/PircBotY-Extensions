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

import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.users.User;

/**
 *
 * @author Joshua
 */
public class ActionEvent extends Event {

    private final Channel channel;
    private final User sender;
    private final String action;

    public ActionEvent(org.pircbotx.hooks.events.ActionEvent event) {
        channel = new Channel(event.getChannel());
        sender = new User(event.getUser());
        action = event.getMessage();
    }

    public String getAction() {
        return action;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getSender() {
        return sender;
    }
}
