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
package net.ae97.pokebot.extensions.links;

import java.util.LinkedList;
import java.util.List;
import net.ae97.pircboty.api.events.MessageEvent;
import net.ae97.pokebot.api.EventExecutor;
import net.ae97.pokebot.api.Listener;
import net.ae97.pokebot.api.Priority;
import net.ae97.pokebot.extension.Extension;

/**
 *
 * @author Joshua
 */
public class BannedLinks extends Extension implements Listener {

    List<String> bannedLinks = new LinkedList<>();
    List<String> activeChannels = new LinkedList<>();

    @Override
    public String getName() {
        return "BannedLinks Extension";
    }

    @Override
    public void load() {
        List<String> list = getConfig().getStringList("banned");
        if (list != null) {
            bannedLinks.addAll(list);
        }
        List<String> channels = getConfig().getStringList("channels");
        if (channels != null) {
            activeChannels.addAll(channels);
        }
    }

    @EventExecutor(priority = Priority.LOW)
    public void runEvent(MessageEvent event) {
        if (!activeChannels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        String message = event.getMessage().toLowerCase();
        for (String link : bannedLinks) {
            if (message.contains(link)) {
                event.getChannel().send().kick(event.getUser(), "Those links are not permitted");
                return;
            }
        }
    }

}
