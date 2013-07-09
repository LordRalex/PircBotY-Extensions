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

import net.ae97.ralexbot.api.EventField;
import net.ae97.ralexbot.api.EventType;
import net.ae97.ralexbot.api.Listener;
import net.ae97.ralexbot.api.Utilities;
import net.ae97.ralexbot.api.events.CommandEvent;
import net.ae97.ralexbot.api.events.JoinEvent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Joshua
 */
public class SayBackCommand extends Listener {

    private Map<String, String> mappings = new HashMap<>();

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        String message = mappings.get(event.getChannel().getName().toLowerCase());
        if (message == null || message.isEmpty()) {
            return;
        }
        event.getChannel().sendMessage(event.getUser().getNick() + ", " + message);
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.getUser().hasOP(event.getChannel().getName()) || event.getUser().hasPermission(event.getChannel().getName(), "saymessage.set")) {
            if (event.getArgs().length == 0) {
                mappings.remove(event.getChannel().getName().toLowerCase());
                event.getChannel().sendMessage("I will stop telling people when they join");
            } else {
                mappings.put(event.getChannel().getName().toLowerCase(), Utilities.toString(event.getArgs()));
                event.getChannel().sendMessage("I will start telling people when they join");
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "message"
        };
    }
}
