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
package net.ae97.pokebot.extensions.scrolls;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.logging.Level;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;

/**
 * @author Lord_Ralex
 */
public class ScrollsExtension extends Extension {

    @Override
    public String getName() {
        return "Scrolls";
    }

    @Override
    public void load() {
        LinkedList<CommandExecutor> executors = new LinkedList<>();
        try {
            executors.add(new OnlineCommand());
        } catch (MalformedURLException ex) {
            PokeBot.getLogger().log(Level.SEVERE, "Could not add .online", ex);
        }
        try {
            executors.add(new StatCommand());
        } catch (MalformedURLException ex) {
            PokeBot.getLogger().log(Level.SEVERE, "Could not add .stats", ex);
        }
        executors.add(new ScrollCommand());
        executors.add(new PlayerCommand(this));
        executors.add(new PriceCommand());
        executors.add(new ExperimentalPriceCommand());

        for (CommandExecutor executor : executors) {
            PokeBot.getExtensionManager().addCommandExecutor(executor);
        }
    }
}
