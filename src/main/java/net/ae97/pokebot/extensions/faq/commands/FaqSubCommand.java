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
package net.ae97.pokebot.extensions.faq.commands;

import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.extensions.faq.FaqExtension;
import net.ae97.pokebot.extensions.faq.database.Database;

/**
 * @author Lord_Ralex
 */
public interface FaqSubCommand {

    public String[] getPrefix();

    public void execute(FaqExtension extension, CommandEvent event, Database database);

    public String[] getHelp();

}
