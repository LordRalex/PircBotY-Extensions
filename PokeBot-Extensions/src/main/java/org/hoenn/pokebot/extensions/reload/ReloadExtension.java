/*
 * Copyright (C) 2014 Lord_Ralex
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
package org.hoenn.pokebot.extensions.reload;

import java.util.logging.Level;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.CommandExecutor;
import org.hoenn.pokebot.api.Utilities;
import org.hoenn.pokebot.api.events.CommandEvent;
import org.hoenn.pokebot.extension.Extension;
import org.hoenn.pokebot.extension.ExtensionReloadFailedException;

/**
 * @author Lord_Ralex
 */
public class ReloadExtension extends Extension implements CommandExecutor {

    @Override
    public String getName() {
        return "Reload Extension";
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (!event.getUser().hasPermission(null, "extension.reload")) {
            return;
        }
        if (event.getArgs().length == 0) {
            return;
        }
        String extensionName = Utilities.toString(event.getArgs());
        Extension extension = PokeBot.getExtensionManager().getExtension(extensionName);
        if (extension == null) {
            event.getUser().sendNotice("No extension found with name '" + extensionName + "'");
        } else {
            try {
                extension.reload();
                event.getUser().sendNotice("'" + extension.getName() + "' has been reloaded");
            } catch (ExtensionReloadFailedException ex) {
                getLogger().log(Level.SEVERE, "Error on reloading " + extension.getName(), ex);
                event.getUser().sendNotice("Error on reloading '" + extensionName + "': " + ex.getClass().getSimpleName());
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"reload"};
    }
}
