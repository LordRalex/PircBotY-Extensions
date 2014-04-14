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
package org.hoenn.pokebot.extensions.taken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.CommandExecutor;
import org.hoenn.pokebot.api.events.CommandEvent;
import org.hoenn.pokebot.extension.Extension;

/**
 * @author Lord_Ralex
 */
public class TakenCommand extends Extension implements CommandExecutor {

    private final String takenURL = "https://account.minecraft.net/buy/frame/checkName/";

    @Override
    public void load() {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (event.getArgs().length != 1) {
            if (event.getChannel() != null) {
                event.getChannel().sendMessage(event.getUser().getNick() + ", usage: taken [username]");
            } else {
                event.getChannel().sendNotice("Usage is: taken [username]");
            }
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(takenURL + event.getArgs()[0]).openStream()))) {
            event.getChannel().sendMessage(event.getUser().getNick() + ", the username " + event.getArgs()[0] + " returned: " + reader.readLine());
        } catch (IOException ex) {
            PokeBot.log(Level.SEVERE, "Error on reading taken status", ex);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "taken"
        };
    }
}
