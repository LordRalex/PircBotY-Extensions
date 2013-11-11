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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.hoenn.pokebot.api.EventType;
import org.hoenn.pokebot.api.Listener;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.events.ActionEvent;
import org.hoenn.pokebot.api.users.BotUser;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.settings.Settings;

/**
 * @author Lord_Ralex
 */
public class TalkingSystem implements Listener {

    private final List<String> messages = new ArrayList<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final List<String> channels = new ArrayList<>();

    public TalkingSystem() {
        Settings settings = new Settings(new File("settings", "taking.yml"));
        List<String> temp = settings.getStringList("channels");
        if (temp != null) {
            channels.addAll(temp);
        }
        temp = settings.getStringList("messages");
        if (temp != null) {
            messages.addAll(temp);
        }
    }

    @EventType
    public void runEvent(ActionEvent event) {
        if (!channels.contains(event.getChannel().getName()) && event.getAction().equalsIgnoreCase("pets " + BotUser.getBotUser().getNick())) {
            return;
        }
        String reply;
        switch (new Random().nextInt(5)) {
            case 0:
                reply = null;
                break;
            case 1:
                reply = "purrs";
                break;
            case 2:
                reply = "leaps away";
                break;
            case 3:
                reply = "growls";
                break;
            case 4:
                reply = "bites {user}";
                break;
            default:
                reply = null;
                break;
        }
        if (reply == null || reply.isEmpty()) {
            return;
        }
        event.getChannel().sendAction(reply.replace("{user}", event.getUser().getNick()));
    }

    private String replaceInto(String string, Map<String, Object> options) {
        String result = string;
        for (Entry<String, Object> entry : options.entrySet()) {
            String value;
            if (entry.getValue() instanceof User) {
                value = ((User) entry.getValue()).getNick();
            } else if (entry.getValue() instanceof Channel) {
                value = ((Channel) entry.getValue()).getName();
            } else {
                value = entry.getValue().toString();
            }
            result = result.replace("{" + entry.getKey() + "}", value);
        }
        return result;
    }

    private final class SpeakRunnable implements Runnable {

        @Override
        public void run() {
            String message;
            synchronized (messages) {
                message = messages.get(new Random().nextInt(messages.size())).trim();
            }
            synchronized (channels) {
                for (String chan : channels) {
                    Channel channel = Channel.getChannel(chan);
                    if (message.startsWith("ACTION")) {
                        channel.sendAction(message.substring("ACTION".length()).trim());
                    } else {
                        channel.sendMessage(message);
                    }
                }
            }
        }
    }
}
