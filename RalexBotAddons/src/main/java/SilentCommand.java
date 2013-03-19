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

import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import java.util.ArrayList;
import java.util.List;

public class SilentCommand extends Listener {

    List<String> silenced = new ArrayList<>();

    @Override
    @EventType(event = EventField.Command, priority = Priority.HIGH)
    public void runEvent(CommandEvent event) {
        if (!event.getSender().hasOP(event.getChannel().getName())) {
            return;
        }
        if (event.getCommand().equalsIgnoreCase("silent")) {
            if (event.getArgs().length == 0 && event.getChannel() != null) {
                String channel = event.getChannel().getName().toLowerCase();
                silenced.remove(channel);
                silenced.add(channel);
                event.getChannel().sendMessage("I am now going into quiet mode");
            } else {
                String channel = event.getArgs()[0].toLowerCase();
                if (!channel.startsWith("#")) {
                    channel = "#" + channel;
                }
                silenced.remove(channel);
                silenced.add(channel);
                event.getSender().sendMessage("I am now going into quiet mode for channel: " + channel);
            }
        }


        if (event.getCommand().equalsIgnoreCase("unsilent")) {
            if (event.getArgs().length == 0 && event.getChannel() != null) {
                String channel = event.getChannel().getName().toLowerCase();
                silenced.remove(channel);
                event.getChannel().sendMessage("I am now going into loud mode");
            } else {
                String channel = event.getArgs()[0].toLowerCase();
                if (!channel.startsWith("#")) {
                    channel = "#" + channel;
                }
                silenced.remove(channel);
                silenced.add(channel);
                event.getSender().sendMessage("I am now going into loud mode for channel: " + channel);
            }
        }


        if (event.getChannel() != null && silenced.contains(event.getChannel().getName().toLowerCase())) {
            boolean canUse = false;
            if (event.getSender().hasVoice(event.getChannel().getName()) || event.getSender().hasOP(event.getChannel().getName())) {
                canUse = true;
            }
            if (!canUse) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "silent",
            "unsilent"
        };
    }
}
