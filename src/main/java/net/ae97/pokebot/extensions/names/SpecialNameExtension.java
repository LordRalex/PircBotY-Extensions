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
package net.ae97.pokebot.extensions.names;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.ae97.pircboty.Channel;
import net.ae97.pircboty.User;
import net.ae97.pircboty.api.events.JoinEvent;
import net.ae97.pircboty.api.events.NickChangeEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.EventExecutor;
import net.ae97.pokebot.api.Listener;
import net.ae97.pokebot.api.Priority;
import net.ae97.pokebot.extension.Extension;
import net.ae97.pokebot.extension.ExtensionReloadFailedException;

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
        return "Special Name";
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
                Set<Channel> chans = event.getUser().getChannels();
                for (Channel chan : chans) {
                    if (channelsToAffect.contains(chan.getName().toLowerCase())) {
                        handleNick(chan, event.getUser(), nope);
                    }
                }
            }
        }
    }

    @EventExecutor(priority = Priority.LOW)
    public void runEvent(JoinEvent event) {
        for (String nope : notAllowed) {
            if (event.getUser().getNick().toLowerCase().contains(nope)) {
                Set<Channel> chans = event.getUser().getChannels();
                for (Channel chan : chans) {
                    if (channelsToAffect.contains(chan.getName().toLowerCase())) {
                        handleNick(chan, event.getUser(), nope);
                    }
                }
            }
        }
    }

    private void handleNick(Channel chan, User user, String string) {
        String name = user.getLogin();
        if (name != null && name.equals(user.getNick())) {
            return;
        }
        String ban = "*" + string + "*!*@" + user.getHostmask();
        chan.send().ban(ban);
        chan.send().kick(user, kickMessage);
        UnbanTask timer = new UnbanTask(chan, ban);
        PokeBot.getScheduler().scheduleTask(timer, unbanDelay, TimeUnit.MINUTES);
    }
}
