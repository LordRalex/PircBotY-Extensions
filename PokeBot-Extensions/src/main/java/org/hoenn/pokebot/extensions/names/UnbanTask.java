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
package org.hoenn.pokebot.extensions.names;

import org.hoenn.pokebot.api.users.BotUser;

/**
 * @author Lord_Ralex
 */
public class UnbanTask implements Runnable {

    private final String unbanLine;
    private final String channel;

    public UnbanTask(String chan, String nick, String name, String ip) {
        channel = chan;
        unbanLine = nick + "!" + name + "@" + ip;
    }

    @Override
    public void run() {
        BotUser.getBotUser().unban(channel, unbanLine);
    }
}
