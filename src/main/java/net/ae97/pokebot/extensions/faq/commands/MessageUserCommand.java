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

import net.ae97.pircboty.Channel;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.extensions.faq.FaqExtension;
import net.ae97.pokebot.extensions.faq.MessageTask;
import net.ae97.pokebot.extensions.faq.database.Database;

/**
 * @author Lord_Ralex
 */
public class MessageUserCommand implements FaqSubCommand {

    @Override
    public String[] getPrefix() {
        return new String[]{
            ">>", "->"
        };
    }

    @Override
    public void execute(FaqExtension extension, CommandEvent event, Database index) {
        if (event.getArgs().length != 2) {
            for (String s : getHelp()) {
                event.getUser().send().notice(s);
            }
            return;
        }
        String target = event.getArgs()[0];
        String factoid = event.getArgs()[1].toLowerCase();
        if (index == null) {
            event.getUser().send().notice("No database is selected");
            return;
        }
        Channel channel = event.getChannel();
        String[] lines = index.getEntry(new String[]{factoid});
        if (lines == null || lines.length == 0) {
            event.getUser().send().notice("The database " + index.getName() + " does not contain a factoid in the categories: " + factoid);
            return;
        }
        if (lines[0].equalsIgnoreCase("@deprecated")) {
            event.getUser().send().notice("The factoid " + factoid + " has been deprecated");
            for (int i = 1; i < lines.length; i++) {
                event.getUser().send().notice(lines[i]);
            }
            return;
        }
        MessageTask thread = new MessageTask(factoid, target, channel, lines, true, target == null ? extension.getMessageFormatNoUser() : extension.getMessageFormat(), extension.getDelay());
        thread.start();
    }

    @Override
    public String[] getHelp() {
        return new String[]{
            "This sends a factoid to a user using the following format: >> [user] [factoid]"
        };
    }

}
