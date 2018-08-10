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
package net.ae97.pokebot.extensions.welcomemessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.ae97.pircboty.User;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pircboty.api.events.JoinEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.api.EventExecutor;
import net.ae97.pokebot.api.Listener;
import net.ae97.pokebot.extension.Extension;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lord_Ralex
 */
public class WelcomeMessageExtension extends Extension implements Listener, CommandExecutor {

    private final Map<String, String> mappings = new HashMap<>();

    @Override
    public String getName() {
        return "WelcomeMessage";
    }

    @Override
    public void load() {
        PokeBot.getExtensionManager().addCommandExecutor(this);
        PokeBot.getExtensionManager().addListener(this);
    }

    @EventExecutor
    public void runEvent(final JoinEvent event) {
        String message = mappings.get(event.getChannel().getName().toLowerCase());
        if (message == null || message.isEmpty()) {
            return;
        }
        final String[] parts = message.split(";;");
        PokeBot.getScheduler().scheduleTask(() -> {
            for (String part : parts) {
                event.getUser().send().notice(part.replace("{user}", event.getUser().getNick()).replace("{channel}", event.getChannel().getName()));
            }
        }, 2, TimeUnit.SECONDS);

    }

    @Override
    public void runEvent(CommandEvent event) {
        Set<User> allowedUsers = new HashSet<>();
        allowedUsers.addAll(event.getChannel().getVoices());
        allowedUsers.addAll(event.getChannel().getOps());
        if (allowedUsers.contains(event.getUser())) {
            if (event.getArgs().length == 0) {
                mappings.remove(event.getChannel().getName().toLowerCase());
                event.getUser().send().notice("I will stop messaging people when they join");
            } else {
                String msg = StringUtils.join(event.getArgs(), " ");
                mappings.put(event.getChannel().getName().toLowerCase(), msg);
                event.getUser().send().notice("I will start messaging people this when they join: ");
                event.getUser().send().notice(msg);
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "message"
        };
    }
}
