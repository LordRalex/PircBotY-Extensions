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
package net.ae97.aebot.api.events;

import net.ae97.aebot.api.channels.Channel;
import net.ae97.aebot.api.users.User;

public class JoinEvent implements UserEvent, ChannelEvent, CancellableEvent {

    private final Channel channel;
    private final User sender;
    private boolean isCancelled = false;

    public JoinEvent(org.pircbotx.hooks.events.JoinEvent event) {
        channel = Channel.getChannel(event.getChannel());
        sender = User.getUser(event.getUser());
    }

    public String getHostname() {
        return sender.getIP();
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
}
