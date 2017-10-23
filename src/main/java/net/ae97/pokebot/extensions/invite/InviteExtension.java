/*
 * Copyright (C) 2015 Joshua
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
package net.ae97.pokebot.extensions.invite;

import net.ae97.pircboty.api.events.InviteEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.EventExecutor;
import net.ae97.pokebot.api.Listener;
import net.ae97.pokebot.extension.Extension;

/**
 *
 * @author Joshua
 */
public class InviteExtension extends Extension implements Listener{

    @Override
    public String getName() {
        return "Invite";
    }

    @Override
    public void load() {
        PokeBot.getEventHandler().registerListener(this);
    }

    @EventExecutor
    public void onInvite(InviteEvent event) {
        String chan = event.getChannel();
        event.getBot().sendIRC().joinChannel(chan);
    }
}
