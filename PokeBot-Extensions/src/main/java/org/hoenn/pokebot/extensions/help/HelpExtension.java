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
package org.hoenn.pokebot.extensions.help;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.CommandExecutor;
import org.hoenn.pokebot.api.events.CommandEvent;
import org.hoenn.pokebot.api.sender.Sender;
import org.hoenn.pokebot.extension.Extension;
import org.hoenn.pokebot.settings.Settings;

/**
 * @author Lord_Ralex
 */
public class HelpExtension extends Extension implements CommandExecutor {

    private String[] help;

    @Override
    public void load() {
        List<String> helpLines;
        try {
            Settings settings = new Settings();
            settings.load(new File("configs", "help.yml"));
            helpLines = settings.getStringList("help-list");
        } catch (IOException ex) {
            PokeBot.log(Level.SEVERE, "Error loading settings file, disabling", ex);
            return;
        }
        help = helpLines.toArray(new String[0]);
        PokeBot.getInstance().getExtensionManager().addCommandExecutor(this);
    }

    @Override
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
