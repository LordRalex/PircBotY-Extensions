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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.EventExecutor;
import org.hoenn.pokebot.api.Listener;
import org.hoenn.pokebot.api.Priority;
import org.hoenn.pokebot.api.events.JoinEvent;
import org.hoenn.pokebot.api.events.NickChangeEvent;
import org.hoenn.pokebot.api.users.BotUser;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.extension.Extension;
import org.hoenn.pokebot.settings.Settings;

/**
 * @author Lord_Ralex
 */
public class SpecialNameExtension extends Extension implements Listener {

    private int unbanDelay;
    private final List<String> notAllowed = new ArrayList<>();
    private final List<String> channelsToAffect = new ArrayList<>();
    private String kickMessage;
    private Settings settings;

    @Override
    public void load() {
        settings = new Settings();
        try {
            settings.load(new File("configs", "specialnames.yml"));
        } catch (IOException ex) {
            PokeBot.log(Level.SEVERE, "Error loading settings file, disabling", ex);
            return;
        }
        List<String> temp = settings.getStringList("nicks");
        if (temp != null) {
            for (String name : temp) {
                notAllowed.add(name.toLowerCase());
            }
        }
        temp = settings.getStringList("channels");
        if (temp != null) {
            for (String name : temp) {
                channelsToAffect.add(name.toLowerCase());
            }
        }
        unbanDelay = settings.getInt("delay");
        kickMessage = settings.getString("kickmessage");
        PokeBot.getInstance().getExtensionManager().addListener(this);
    }

    @EventExecutor(priority = Priority.LOW)
    public void runEvent(NickChangeEvent event) {
        for (String nope : notAllowed) {
            if (event.getNewNick().toLowerCase().contains(nope)) {
                String[] chans = event.getUser().getChannels();
                for (String chan : chans) {
                    if (channelsToAffect.contains(chan.toLowerCase())) {
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
                String[] chans = event.getUser().getChannels();
                for (String chan : chans) {
                    if (channelsToAffect.contains(chan.toLowerCase())) {
                        handleNick(chan, event.getUser(), nope);
                    }
                }
            }
        }
    }

    private void handleNick(String chan, User user, String string) {
        String name = user.isVerified();
        if (name != null && name.equals(user.getNick())) {
            return;
        }
        String ban = "*" + string + "*!*@" + user.getIP();
        BotUser.getBotUser().ban(chan, ban);
        BotUser.getBotUser().kick(user.getNick(), chan, kickMessage);
        UnbanTask timer = new UnbanTask(chan, "*" + user.getNick() + "*", "*", user.getIP());
        PokeBot.getInstance().getScheduler().scheduleTask(timer, unbanDelay, TimeUnit.MINUTES);
    }
}
