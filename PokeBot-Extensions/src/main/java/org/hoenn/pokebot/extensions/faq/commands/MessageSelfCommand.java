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
package org.hoenn.pokebot.extensions.faq.commands;

import org.hoenn.pokebot.api.events.CommandEvent;
import org.hoenn.pokebot.extensions.faq.FaqExtension;
import org.hoenn.pokebot.extensions.faq.MessageTask;
import org.hoenn.pokebot.extensions.faq.database.Database;

/**
 * @author Lord_Ralex
 */
public class MessageSelfCommand implements FaqSubCommand {

    @Override
    public String[] getPrefix() {
        return new String[]{
            "<", "<<"
        };
    }

    @Override
    public void execute(FaqExtension extension, CommandEvent event) {
        if (event.getArgs().length != 1) {
            event.getUser().sendNotice(getHelp());
            return;
        }
        String target = event.getUser().getNick();
        String factoid = event.getArgs()[0].toLowerCase();
        Database index = extension.getDatabase(event);
        if (index == null) {
            event.getUser().sendNotice("No database is selected");
            return;
        }
        String channel = event.getChannel().getName();
        String[] lines = index.getEntry(extension.splitFactoid(factoid));
        if (lines == null || lines.length == 0) {
            event.getUser().sendNotice("The database " + index.getName() + " does not contain a factoid in the categories: " + factoid);
            return;
        }
        if (lines[0].equalsIgnoreCase("@deprecated")) {
            event.getUser().sendNotice("The factoid " + factoid + " has been deprecated");
            for (int i = 1; i < lines.length; i++) {
                event.getUser().sendNotice(lines[i]);
            }
            return;
        }
        MessageTask thread = new MessageTask(factoid, target, channel, lines, true, target == null ? extension.getMessageFormatNoUser() : extension.getMessageFormat(), extension.getDelay());
        thread.start();
    }

    @Override
    public String[] getHelp() {
        return new String[]{
            "This command messages a factoid to you using the following format: ??< [factoid]"
        };
    }
}
