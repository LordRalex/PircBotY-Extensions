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
package net.ae97.aebot.api.events;

import net.ae97.aebot.api.channels.Channel;
import net.ae97.aebot.api.users.User;

/**
 * @version 1.0
 * @author Laptop
 */
public class KickEvent implements CancellableEvent, UserEvent, ChannelEvent {

    private final User kicker;
    private final User user;
    private final Channel channel;
    private final String message;
    private boolean isCancelled = false;

    public KickEvent(org.pircbotx.hooks.events.KickEvent event) {
        kicker = User.getUser(event.getRecipient());
        user = User.getUser(event.getSource());
        channel = Channel.getChannel(event.getChannel());
        message = event.getReason();
    }

    public User getKicker() {
        return kicker;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public User getUser() {
        return user;
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
