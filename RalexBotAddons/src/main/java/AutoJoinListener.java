
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.KickEvent;
import com.lordralex.ralexbot.api.users.BotUser;
import com.lordralex.ralexbot.settings.Settings;
import com.lordralex.ralexbot.threads.DelayedTask;
import java.util.HashMap;
import java.util.Map;

/*
 * Copyright (C) 2013 Laptop
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
 * @author Laptop
 */
public class AutoJoinListener extends Listener {

    private int delay, delay_clean;
    private final Map<String, Long> lastKicked = new HashMap<>();

    @Override
    public void setup() {
        delay = Settings.getGlobalSettings().getInt("unban-delay-autojoin");
        delay_clean = Settings.getGlobalSettings().getInt("unban-delay-autojoin-cleaner");
    }

    @Override
    @EventType(event = EventField.Kick, priority = Priority.LOW)
    public void runEvent(KickEvent event) {
    }

    @Override
    @EventType(event = EventField.Join, priority = Priority.LOW)
    public void runEvent(JoinEvent event) {
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

    private class Cleaner extends Thread {

        @Override
        public void run() {
            boolean run = true;
            while (run) {
                synchronized (this) {
                    try {
                        this.wait(delay_clean);
                    } catch (InterruptedException e) {
                        run = false;
                    }
                }
                synchronized (lastKicked) {
                    for (String name : lastKicked.keySet()) {
                        Long timeago = lastKicked.get(name);
                        if (timeago + (delay * 2) > System.currentTimeMillis()) {
                            lastKicked.remove(name);
                        }
                    }
                }
            }
        }
    }
}
