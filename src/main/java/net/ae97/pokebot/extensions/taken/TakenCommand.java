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
package net.ae97.pokebot.extensions.taken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.extension.Extension;

/**
 * @author Lord_Ralex
 */
public class TakenCommand extends Extension implements CommandExecutor {

    private final String takenURL = "https://account.minecraft.net/buy/frame/checkName/";

    @Override
    public String getName() {
        return "Taken";
    }

    @Override
    public void load() {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (event.getArgs().length != 1) {
            if (event.getChannel() != null) {
                event.respond("usage: taken [username]");
            } else {
                event.getChannel().send().notice("Usage is: taken [username]");
            }
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(takenURL + event.getArgs()[0]).openStream()))) {
            event.respond("the username " + event.getArgs()[0] + " returned: " + reader.readLine());
        } catch (IOException ex) {
            PokeBot.getLogger().log(Level.SEVERE, "Error on reading taken status", ex);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "taken"
        };
    }
}
