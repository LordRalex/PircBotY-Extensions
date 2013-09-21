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

import net.ae97.aebot.AeBot;
import net.ae97.aebot.api.CommandExecutor;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.sender.Sender;
import net.ae97.aebot.api.users.BotUser;
import org.pircbotx.PircBotX;

public class InfoCommand extends CommandExecutor {

    @Override
    public void runEvent(CommandEvent event) {
        Sender target = event.getChannel();
        if (target == null) {
            target = event.getUser();
        }
        target.sendMessage("Hello. I am " + BotUser.getBotUser().getNick() + " " + AeBot.VERSION + " using PircBotX " + PircBotX.VERSION);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            BotUser.getBotUser().getNick().toLowerCase(),
            "version"
        };
    }
}
