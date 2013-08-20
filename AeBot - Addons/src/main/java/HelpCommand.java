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
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.sender.Sender;
import net.ae97.aebot.settings.Settings;
import java.io.File;
import java.util.List;

public class HelpCommand extends Listener {

    private String[] help;

    @Override
    public void onLoad() {
        List<String> helpLines = new Settings(new File("settings", "help.yml")).getStringList("help-list");
        help = helpLines.toArray(new String[0]);
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Sender target = event.getChannel();
        if (target == null) {
            target = event.getUser();
        }
        if (target == null) {
            return;
        }
        String helpLine = "My commands you can know about: ";
        for (String name : help) {
            helpLine += name + ", ";
        }
        helpLine = helpLine.trim();
        if (helpLine.endsWith(",")) {
            helpLine = helpLine.substring(0, helpLine.length() - 2);
        }
        target.sendMessage(event.getUser().getNick() + ", " + helpLine);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "help",
            "commands"
        };
    }
}
