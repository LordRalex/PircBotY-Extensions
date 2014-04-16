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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.EventExecutor;
import org.hoenn.pokebot.api.Listener;
import org.hoenn.pokebot.api.Priority;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.events.JoinEvent;
import org.hoenn.pokebot.api.events.NickChangeEvent;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.extension.Extension;
import org.hoenn.pokebot.extension.ExtensionReloadFailedException;

/**
 * @author Lord_Ralex
 */
public class SpecialNameExtension extends Extension implements Listener {

    private int unbanDelay;
    private final List<String> notAllowed = new ArrayList<>();
    private final List<String> channelsToAffect = new ArrayList<>();
    private String kickMessage;

    @Override
    public String getName() {
        return "Special Name Extension";
    }

    @Override
    public void load() {
        List<String> temp = getConfig().getStringList("nicks");
        if (temp != null) {
            for (String name : temp) {
                notAllowed.add(name.toLowerCase());
            }
        }
        temp = getConfig().getStringList("channels");
        if (temp != null) {
            for (String name : temp) {
                channelsToAffect.add(name.toLowerCase());
            }
        }
        unbanDelay = getConfig().getInt("delay");
        kickMessage = getConfig().getString("kickmessage");
        PokeBot.getExtensionManager().addListener(this);
    }

    @Override
    public void reload() throws ExtensionReloadFailedException {
        super.reload();
        notAllowed.clear();
        channelsToAffect.clear();
        List<String> temp = getConfig().getStringList("nicks");
        if (temp != null) {
            for (String name : temp) {
                notAllowed.add(name.toLowerCase());
            }
        }
        temp = getConfig().getStringList("channels");
        if (temp != null) {
            for (String name : temp) {
                channelsToAffect.add(name.toLowerCase());
            }
        }
        unbanDelay = getConfig().getInt("delay");
        kickMessage = getConfig().getString("kickmessage");
    }

    @EventExecutor(priority = Priority.LOW)
    public void runEvent(NickChangeEvent event) {
        for (String nope : notAllowed) {
            if (event.getNewNick().toLowerCase().contains(nope)) {
                String[] chans = event.getUser().getChannels();
                for (String chan : chans) {
                    if (channelsToAffect.contains(chan.toLowerCase())) {
                        handleNick(PokeBot.getChannel(chan), event.getUser(), nope);
                    }
                }
            }
        }
    }

    @EventExecutor(priority = Priority.LOW)
    public void runEvent(JoinEvent event) {
        for (String nope : notAllowed) {
            if (event.getUser().getNick().toLowerCase().contains(nope)) {
                String[] chans = event.getUser().getChannels();
                for (String chan : chans) {
                    if (channelsToAffect.contains(chan.toLowerCase())) {
                        handleNick(PokeBot.getChannel(chan), event.getUser(), nope);
                    }
                }
            }
        }
    }

    private void handleNick(Channel chan, User user, String string) {
        String name = user.getNickservName();
        if (name != null && name.equals(user.getNick())) {
            return;
        }
        String ban = "*" + string + "*!*@" + user.getHost();
        chan.ban(ban);
        chan.kickUser(user.getNick(), kickMessage);
        UnbanTask timer = new UnbanTask(chan, "" + user.getNick() + "!*@" + user.getHost());
        PokeBot.getScheduler().scheduleTask(timer, unbanDelay, TimeUnit.MINUTES);
    }
}
