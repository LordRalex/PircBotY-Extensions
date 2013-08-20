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

import net.ae97.aebot.api.EventField;
import net.ae97.aebot.api.EventType;
import net.ae97.aebot.api.Listener;
import net.ae97.aebot.api.events.JoinEvent;
import net.ae97.aebot.api.users.User;

public class GuestListener extends Listener {

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        if (event.isCancelled()) {
            return;
        }
        User sender = event.getUser();
        String nick = sender.getNick().toLowerCase().trim();
        if (nick.startsWith("guest") || nick.startsWith("mib_")) {
            sender.sendNotice("Please use /nick <name> to change your name");
            sender.sendNotice("I am only a bot though, so i will not reply to your pms");
        }
    }
}
