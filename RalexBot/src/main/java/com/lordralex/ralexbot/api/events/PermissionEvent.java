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
 * @author Lord_Ralex
 * @version 1.0
 */
public class PermissionEvent extends Event {

    private final User user;
    private final Channel channel;

    public PermissionEvent(User u, Channel c) {
        user = u;
        channel = c;
    }

    public PermissionEvent(String u, String c) {
        this(new User(u), new Channel(c));
    }

    public User getUser() {
        return user;
    }

    public Channel getChannel() {
        return channel;
    }
}
