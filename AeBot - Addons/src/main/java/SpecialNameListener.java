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

import net.ae97.aebot.api.EventType;
import net.ae97.aebot.api.Listener;
import net.ae97.aebot.api.Priority;
import net.ae97.aebot.api.events.JoinEvent;
import net.ae97.aebot.api.events.NickChangeEvent;
import net.ae97.aebot.api.users.BotUser;
import net.ae97.aebot.api.users.User;
import net.ae97.aebot.settings.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class SpecialNameListener implements Listener {

    private final int unbanDelay;
    private final List<String> notAllowed = new ArrayList<>();
    private final List<String> channelsToAffect = new ArrayList<>();
    private final ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
    private final String kickMessage;
    private final Settings settings;

    public SpecialNameListener() {
        settings = new Settings(new File("settings", "specialnames.yml"));
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
    }

    @EventType(priority = Priority.LOW)
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

    @EventType(priority = Priority.LOW)
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
        UnbanTimer timer = new UnbanTimer(chan, "*" + user.getNick() + "*", "*", user.getIP());
        es.schedule(timer, unbanDelay, TimeUnit.MINUTES);
    }

    private class UnbanTimer implements Runnable {

        private final String unbanLine;
        private final String channel;

        public UnbanTimer(String chan, String nick, String name, String ip) {
            channel = chan;
            unbanLine = nick + "!" + name + "@" + ip;
        }

        @Override
        public void run() {
            BotUser.getBotUser().unban(channel, unbanLine);
        }
    }
}
