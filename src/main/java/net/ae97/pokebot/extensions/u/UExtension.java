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
package net.ae97.pokebot.extensions.u;

import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;

/**
 * @author Lord_Ralex
 */
public class UExtension extends Extension implements CommandExecutor {

    @Override
    public String getName() {
        return "U";
    }

    @Override
    public void load() {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (event.getArgs().length == 0) {
            event.respond("$u <user> [profile, posts, topics, infractions, pm, names, admin, edit, modcp, warn, ip_history]");
        } else {
            String link = "http://u.mcf.li/" + event.getArgs()[0];
            if (event.getArgs().length >= 2) {
                link += "/" + event.getArgs()[1];
            }
            event.respond(link);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "u"
        };
    }

}
