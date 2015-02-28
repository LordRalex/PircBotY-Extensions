/*
 * Copyright (C) 2015 Lord_Ralex
 *
 * This file is a part of pokebot-extensions
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
package net.ae97.pokebot.extensions.bans;

import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.extension.Extension;

/**
 * @author Joshua
 */
public class BanSystem extends Extension {

    @Override
    public String getName() {
        return "BanSystem";
    }

    @Override
    public void load() {
        PokeBot.getEventHandler().registerListener(new BanSystemListener(this));
    }

}
