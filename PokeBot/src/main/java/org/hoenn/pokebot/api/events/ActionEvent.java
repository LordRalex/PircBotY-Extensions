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

import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.users.User;

/**
 *
 * @author Joshua
 */
public class ActionEvent implements UserEvent, ChannelEvent, CancellableEvent {

    private final Channel channel;
    private final User sender;
    private final String action;
    private boolean isCancelled = false;
    private final long timestamp = System.currentTimeMillis();

    public ActionEvent(org.pircbotx.hooks.events.ActionEvent event) {
        channel = Channel.getChannel(event.getChannel());
        sender = User.getUser(event.getUser());
        action = event.getMessage();
    }

    public String getAction() {
        return action;
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
}
