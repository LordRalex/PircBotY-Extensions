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
package net.ae97.pokebot.extensions.reload;

import java.util.logging.Level;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;
import net.ae97.pokebot.extension.ExtensionReloadFailedException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lord_Ralex
 */
public class ReloadExtension extends Extension implements CommandExecutor {

    @Override
    public String getName() {
        return "Reload";
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (!event.getUser().hasPermission(null, "extension.reload")) {
            return;
        }
        if (event.getArgs().length == 0) {
            return;
        }
        String extensionName = StringUtils.join(event.getArgs(), " ");
        Extension extension = PokeBot.getExtensionManager().getExtension(extensionName);
        if (extension == null) {
            event.getUser().send().notice("No extension found with name '" + extensionName + "'");
        } else {
            try {
                extension.reload();
                event.getUser().send().notice("'" + extension.getName() + "' has been reloaded");
            } catch (ExtensionReloadFailedException ex) {
                getLogger().log(Level.SEVERE, "Error on reloading " + extension.getName(), ex);
                event.getUser().send().notice("Error on reloading '" + extensionName + "': " + ex.getClass().getSimpleName());
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"reload"};
    }
}
