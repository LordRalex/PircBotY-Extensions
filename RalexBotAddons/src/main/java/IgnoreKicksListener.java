
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
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.KickEvent;
import com.lordralex.ralexbot.api.users.BotUser;
import com.lordralex.ralexbot.settings.Settings;
import com.lordralex.ralexbot.threads.DelayedTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IgnoreKicksListener extends Listener {

    private final Map<String, List<Long>> kickedUsers = new ConcurrentHashMap<>();
    private final List<String> ignoredHosts = new ArrayList<>();
    private int max_kicks;
    private int delay;

    @Override
    public void setup() {
        max_kicks = Settings.getGlobalSettings().getInt("max-kicks-ignored");
        delay = Settings.getGlobalSettings().getInt("unban-delay-kick-ignored");
        List<String> hosts = Settings.getGlobalSettings().getStringList("ignored-hosts-autojoin");
        if (hosts != null) {
            ignoredHosts.addAll(hosts);
        }
    }

    @Override
    @EventType(event = EventField.Kick, priority = Priority.LOW)
    public void runEvent(KickEvent event) {
        if (!event.getSender().getNick().equalsIgnoreCase(BotUser.getBotUser().getNick())) {
            return;
        }
        if (ignoredHosts.contains(event.getRecipient().getIP())) {
            return;
        }
        List<Long> oldKicks = kickedUsers.remove(event.getRecipient().getIP());
        if (oldKicks == null) {
            oldKicks = new ArrayList<>();
        }
        oldKicks.add(System.currentTimeMillis());
        if (oldKicks.size() >= max_kicks) {
            BotUser.getBotUser().ban(event.getChannel().getName(), "*!*@" + event.getRecipient().getIP());
            UnbanTimer timer = new UnbanTimer(delay, event.getChannel().getName(), event.getRecipient().getIP());
            timer.start();
        } else {
            kickedUsers.put(event.getRecipient().getIP(), oldKicks);
        }
    }

    private class UnbanTimer extends DelayedTask {

        String channel;
        String ip;

        public UnbanTimer(int timer, String c, String host) {
            super(timer);
            channel = c;
            ip = host;
        }

        public UnbanTimer(String c, String host) {
            this(delay, c, host);
        }

        @Override
        public void runTask() {
            BotUser.getBotUser().unban(channel, "*!*@" + ip);
        }
    }
}
