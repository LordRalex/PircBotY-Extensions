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
package org.hoenn.pokebot.extensions.info;

import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.CommandExecutor;
import org.hoenn.pokebot.api.events.CommandEvent;
import org.hoenn.pokebot.api.recipients.MessageRecipient;
import org.hoenn.pokebot.extension.Extension;
import org.pircbotx.PircBotX;

/**
 * @author Lord_Ralex
 */
public class InfoExtension extends Extension implements CommandExecutor {

    @Override
    public void load() {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        MessageRecipient target = event.getChannel();
        if (target == null) {
            target = event.getUser();
        }
        target.sendMessage("Hello. I am " + PokeBot.getBot().getNick() + ", PokeBot " + PokeBot.VERSION + " using PircBotX " + PircBotX.VERSION);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            PokeBot.getBot().getNick().toLowerCase(),
            "version"
        };
    }

}
