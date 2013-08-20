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

import net.ae97.aebot.api.EventField;
import net.ae97.aebot.api.EventType;
import net.ae97.aebot.api.Listener;
import net.ae97.aebot.api.Priority;
import net.ae97.aebot.api.channels.Channel;
import net.ae97.aebot.api.events.MessageEvent;
import net.ae97.aebot.api.users.BotUser;
import net.ae97.aebot.settings.Settings;
import java.io.File;
import java.util.List;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class RecordedMessage extends Listener {

    int counter = 0;
    private Settings settings;

    @Override
    public void onLoad() {
        settings = new Settings(new File("settings", "recordedmessage.yml"));
    }

    @Override
    @EventType(event = EventField.Message, priority = Priority.LOW)
    public void runEvent(MessageEvent event) {
        if (!event.getUser().hasOP(event.getChannel().getName()) && !event.getUser().hasPermission(event.getChannel().getName(), "recordedmessage.play")) {
            return;
        }
        if (event.getMessage().startsWith(BotUser.getBotUser().getNick() + ", please teach")) {
            String load = event.getMessage().replace(BotUser.getBotUser().getNick() + ", please teach", "").trim();
            load = load.split(" ")[0];
            AutomatedMessageThread thread = new AutomatedMessageThread(load, event.getChannel());
            thread.start();
        }
    }

    private class AutomatedMessageThread extends Thread {

        final List<String> messages;
        final Channel chan;
        final int message_delay;
        boolean stop = false;

        public AutomatedMessageThread(String section, Channel channel) {
            chan = channel;
            chan.sendMessage("Loading " + section);
            messages = settings.getStringList(section);
            message_delay = settings.getInt(section + "_timing");
        }

        @Override
        public void run() {
            while (!isInterrupted() && !messages.isEmpty()) {
                synchronized (this) {
                    try {
                        wait(message_delay * 1000);
                    } catch (InterruptedException ex) {
                    }
                }
                if (!stop && !isInterrupted() && !messages.isEmpty()) {
                    synchronized (messages) {
                        String nextLine = messages.remove(0);
                        chan.sendMessage(nextLine);
                    }
                }
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
            stop = true;
        }

        @Override
        public boolean isInterrupted() {
            if (super.isInterrupted() || stop) {
                return true;
            } else {
                return false;
            }
        }
    }
}
