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
package net.ae97.pokebot.extensions.censor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.ae97.pircboty.api.events.ActionEvent;
import net.ae97.pircboty.api.events.JoinEvent;
import net.ae97.pircboty.api.events.MessageEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.EventExecutor;
import net.ae97.pokebot.api.Listener;
import net.ae97.pokebot.extension.Extension;
import net.ae97.pokebot.extension.ExtensionReloadFailedException;

/**
 * @author Lord_Ralex
 */
public class CensorExtension extends Extension implements Listener {

    private final List<String> censor = new ArrayList<>();
    private final List<String> channels = new ArrayList<>();
    private final Set<String> warned = new HashSet<>();

    @Override
    public String getName() {
        return "Censor";
    }

    @Override
    public void load() {
        censor.clear();
        censor.addAll(getConfig().getStringList("words"));
        channels.clear();
        channels.addAll(getConfig().getStringList("channels"));
        PokeBot.getExtensionManager().addListener(this);
    }

    @Override
    public void reload() throws ExtensionReloadFailedException {
        super.reload();
        censor.clear();
        censor.addAll(getConfig().getStringList("words"));
        channels.clear();
        channels.addAll(getConfig().getStringList("channels"));
    }

    @EventExecutor
    public void runEvent(MessageEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        if (event.getUser().getNick().equalsIgnoreCase(PokeBot.getBot().getNick())) {
            return;
        }
        String message = event.getMessage().toLowerCase();
        if (scanMessage(message)) {
            if (warned.contains(event.getUser().getNick()) || warned.contains(event.getUser().getHostmask())) {
                event.getChannel().send().kick(event.getUser(), getConfig().getString("kickmessage"));
            } else {
                warned.add(event.getUser().getNick());
                warned.add(event.getUser().getHostmask());
                event.respond(getConfig().getString("warnmessage", "Please keep it civil"));
            }
        }
    }

    @EventExecutor
    public void runEvent(ActionEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        if (event.getUser().getNick().equalsIgnoreCase(PokeBot.getBot().getNick())) {
            return;
        }
        if (event.getChannel().getOps().contains(event.getUser())) {
            return;
        }
        if (event.getUser().hasPermission(event.getChannel().getName(), "censor.ignore")) {
            return;
        }
        String message = event.getMessage().toLowerCase();
        if (scanMessage(message)) {
            if (warned.contains(event.getUser().getNick()) || warned.contains(event.getUser().getHostmask())) {
                event.getChannel().send().kick(event.getUser(), getConfig().getString("kickmessage"));
            } else {
                warned.add(event.getUser().getNick());
                warned.add(event.getUser().getHostmask());
                event.respond(getConfig().getString("warnmessage", "Please keep it civil"));
            }
        }
    }

    @EventExecutor
    public void runEvent(JoinEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        String message = event.getUser().getNick().toLowerCase();
        if (scanMessage(message)) {
            event.getChannel().send().kick(event.getUser(), getConfig().getString("kickmessage", "Please get a different nickname"));
        }
    }

    private boolean scanMessage(String message) {
        String test = message.toLowerCase();
        for (String word : censor) {
            if (test.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
