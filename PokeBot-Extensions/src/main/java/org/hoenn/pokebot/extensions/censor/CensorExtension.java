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
package org.hoenn.pokebot.extensions.censor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.EventExecutor;
import org.hoenn.pokebot.api.Listener;
import org.hoenn.pokebot.api.events.ActionEvent;
import org.hoenn.pokebot.api.events.JoinEvent;
import org.hoenn.pokebot.api.events.MessageEvent;
import org.hoenn.pokebot.api.events.NickChangeEvent;
import org.hoenn.pokebot.api.users.BotUser;
import org.hoenn.pokebot.extension.Extension;
import org.hoenn.pokebot.settings.Settings;

/**
 * @author Lord_Ralex
 */
public class CensorExtension extends Extension implements Listener {

    private final List<String> censor = new ArrayList<>();
    private final List<String> channels = new ArrayList<>();
    private final Set<String> warned = new HashSet<>();
    private Settings settings;
    private String warnMessage, kickMessage;

    @Override
    public void load() {
        settings = new Settings(new File("settings", "censor.yml"));
        censor.clear();
        censor.addAll(settings.getStringList("words"));
        channels.clear();
        channels.addAll(settings.getStringList("channels"));
        warnMessage = settings.getString("warnmessage");
        kickMessage = settings.getString("kickmessage");
        PokeBot.getInstance().getExtensionManager().addListener(this);
    }

    @EventExecutor
    public void runEvent(MessageEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        if (event.getUser().getNick().equalsIgnoreCase(BotUser.getBotUser().getNick())) {
            return;
        }
        if (event.getUser().hasOP(event.getChannel().getName())) {
            return;
        }
        if (event.getUser().hasPermission(event.getChannel().getName(), "censor.ignore")) {
            return;
        }
        String message = event.getMessage().toLowerCase();
        if (scanMessage(message)) {
            if (warned.contains(event.getUser().getNick()) || warned.contains(event.getUser().getIP())) {
                BotUser.getBotUser().kick(event.getUser().getNick(), event.getChannel().getName(), kickMessage);
            } else {
                warned.add(event.getUser().getNick());
                warned.add(event.getUser().getIP());
                event.getChannel().sendMessage(warnMessage.replace("{name}", event.getUser().getNick()));
            }
        }
    }

    @EventExecutor
    public void runEvent(ActionEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        if (event.getUser().getNick().equalsIgnoreCase(BotUser.getBotUser().getNick())) {
            return;
        }
        String message = event.getAction().toLowerCase();
        if (scanMessage(message)) {
            if (warned.contains(event.getUser().getNick()) || warned.contains(event.getUser().getIP())) {
                BotUser.getBotUser().kick(event.getUser().getNick(), event.getChannel().getName(), kickMessage);
            } else {
                warned.add(event.getUser().getNick());
                warned.add(event.getUser().getIP());
                event.getChannel().sendMessage(warnMessage.replace("{name}", event.getUser().getNick()));
            }
        }
    }

    @EventExecutor
    public void runEvent(NickChangeEvent event) {
        String message = event.getNewNick().toLowerCase();
        if (scanMessage(message)) {
            for (String chan : BotUser.getBotUser().getChannels()) {
                BotUser.getBotUser().kick(event.getNewNick(), chan, kickMessage);
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
            BotUser.getBotUser().kick(event.getUser().getNick(), event.getChannel().getName(), kickMessage);
        }
    }

    private boolean scanMessage(String message) {
        for (String word : censor) {
            String[] parts = message.split(" ");
            for (String p : parts) {
                if (p.equalsIgnoreCase(word)) {
                    return true;
                }
            }
        }
        return false;
    }
}
