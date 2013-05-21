
import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.api.users.BotUser;
import com.lordralex.ralexbot.api.users.User;
import com.lordralex.ralexbot.settings.Settings;
import com.lordralex.ralexbot.threads.DelayedTask;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final List<String> channelsToAffect = new ArrayList<String>();

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
    }

    @Override
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
        if (user.isVerified()) {
            return;
        }
        String ban = user.getNick() + "!" + "*" + "@" + user.getIP();
        BotUser.getBotUser().ban(chan, ban);
        BotUser.getBotUser().kick(user.getNick(), chan, "Nickname not allowed");
        UnbanTimer timer = new UnbanTimer(chan, user.getNick(), "*", user.getIP());
        timer.start();
    }

    private class UnbanTimer extends DelayedTask {

        private final String unbanLine;
        private final String channel;

        public UnbanTimer(String chan, String nick, String name, String ip) {
            super(unbanDelay);
            channel = chan;
            unbanLine = nick + "!" + name + "@" + ip;
        }

        @Override
        public void runTask() {
            BotUser.getBotUser().unban(channel, unbanLine);
        }
    }
}
