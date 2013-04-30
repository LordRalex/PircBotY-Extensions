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

import com.lordralex.ralexbot.api.users.User;

public class NoticeEvent extends Event {

    private final String message;
    private final User sender;

    public NoticeEvent(org.pircbotx.hooks.events.NoticeEvent event) {
        sender = new User(event.getUser());
        message = event.getMessage();
    }

    public User getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getHostname() {
        return sender.getIP();
    }
}
