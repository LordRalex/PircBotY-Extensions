
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.api.users.BotUser;
import com.lordralex.ralexbot.api.users.User;
import com.lordralex.ralexbot.settings.Settings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class SpecialNameListener extends Listener {

    private int unbanDelay;
    private final List<String> notAllowed = new ArrayList<>();
    private final List<String> channelsToAffect = new ArrayList<>();
    private final ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void setup() {
        List<String> temp = Settings.getGlobalSettings().getStringList("banned-nicks");
        if (temp != null) {
            for (String name : temp) {
                notAllowed.add(name.toLowerCase());
            }
        }
        temp = Settings.getGlobalSettings().getStringList("banned-nicks-channels");
        if (temp != null) {
            for (String name : temp) {
                channelsToAffect.add(name.toLowerCase());
            }
        }
        unbanDelay = Settings.getGlobalSettings().getInt("banned-nicks-delay");
    }

    @Override
    @EventType(event = EventField.NickChange, priority = Priority.LOW)
    public void runEvent(NickChangeEvent event) {
        if (notAllowed.contains(event.getNewNick().toLowerCase())) {
            String[] chans = event.getUser().getChannels();
            for (String chan : chans) {
                if (channelsToAffect.contains(chan.toLowerCase())) {
                    handleNick(chan, event.getUser());
                }
            }
        }
    }

    @Override
    @EventType(event = EventField.Join, priority = Priority.LOW)
    public void runEvent(JoinEvent event) {
        if (notAllowed.contains(event.getSender().getNick().toLowerCase())) {
            String[] chans = event.getSender().getChannels();
            for (String chan : chans) {
                if (channelsToAffect.contains(chan.toLowerCase())) {
                    handleNick(chan, event.getSender());
                }
            }
        }
    }

    private void handleNick(String chan, User user) {
        String name = user.isVerified();
        if (name != null && name.equals(user.getNick())) {
            return;
        }
        String ban = "*" + user.getNick() + "*!*@" + user.getIP();
        BotUser.getBotUser().ban(chan, ban);
        BotUser.getBotUser().kick(user.getNick(), chan, "Nickname not allowed, use another one");
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
