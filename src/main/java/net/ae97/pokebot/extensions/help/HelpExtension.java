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
package net.ae97.pokebot.extensions.help;

import java.util.LinkedList;
import java.util.List;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lord_Ralex
 */
public class HelpExtension extends Extension implements CommandExecutor {

    private final LinkedList<String> help = new LinkedList<>();

    @Override
    public String getName() {
        return "Help";
    }

    @Override
    public void load() {
        List<String> helpLines = getConfig().getStringList("help");
        if (helpLines != null) {
            help.addAll(helpLines);
        }
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        String helpLine = "Commands: ";
        helpLine += StringUtils.join(help, ", ");
        helpLine = helpLine.trim();
        event.respond(helpLine);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "help",
            "commands"
        };
    }
}
