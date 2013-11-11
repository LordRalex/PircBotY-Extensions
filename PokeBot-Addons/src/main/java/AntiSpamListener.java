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

import org.hoenn.pokebot.api.EventType;
import org.hoenn.pokebot.api.Listener;
import org.hoenn.pokebot.api.Priority;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.events.ActionEvent;
import org.hoenn.pokebot.api.events.MessageEvent;
import org.hoenn.pokebot.api.users.BotUser;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.settings.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lord_Ralex
 */
public class AntiSpamListener implements Listener {

    private final Map<String, Posts> logs = new HashMap<>();
    private final int MAX_MESSAGES;
    private final int SPAM_RATE;
    private final int DUPE_RATE;
    private final List<String> channels = new ArrayList<>();
    private final String kickMessage;

    public AntiSpamListener() {
        Settings settings = new Settings(new File("settings", "antispam.yml"));
        MAX_MESSAGES = settings.getInt("message-count");
        SPAM_RATE = settings.getInt("spam-rate");
        DUPE_RATE = settings.getInt("dupe-rate");
        kickMessage = settings.getString("kickmessage");
        channels.clear();
        channels.addAll(settings.getStringList("channels"));
        logs.clear();
    }

    @EventType(priority = Priority.LOW)
    public void runEvent(MessageEvent event) {
        synchronized (logs) {
            Channel channel = event.getChannel();
            if (!channels.contains(channel.getName().toLowerCase())) {
                return;
            }
            User sender = event.getUser();
            String message = event.getMessage();
            if (sender.hasOP(channel.getName()) || sender.hasVoice(channel.getName()) || sender.getNick().equalsIgnoreCase(BotUser.getBotUser().getNick()) || sender.hasPermission(channel.getName(), "antispam.ignore")) {
                return;
            }
            message = message.toString().toLowerCase();
            Posts posts = logs.remove(sender.getNick());
            if (posts == null) {
                posts = new Posts();
            }
            if (posts.addPost(message)) {
                BotUser.getBotUser().kick(sender.getNick(), channel.getName(), kickMessage.replace("{ip}", sender.getIP()));
                event.setCancelled(true);
            } else {
                logs.put(sender.getNick(), posts);
            }
        }
    }

    @EventType(priority = Priority.LOW)
    public void runEvent(ActionEvent event) {
        synchronized (logs) {
            if (event.isCancelled()) {
                return;
            }
            Channel channel = event.getChannel();
            User sender = event.getUser();
            String message = event.getAction();
            if (sender.hasOP(channel.getName()) || sender.hasVoice(channel.getName()) || sender.getNick().equalsIgnoreCase(BotUser.getBotUser().getNick())) {
                return;
            }
            message = message.toString().toLowerCase();
            Posts posts = logs.remove(sender.getNick());
            if (posts == null) {
                posts = new Posts();
            }
            if (posts.addPost(message)) {
                BotUser.getBotUser().kick(sender.getNick(), channel.getName(), kickMessage.replace("{ip}", sender.getIP()));
                event.setCancelled(true);
            } else {
                logs.put(sender.getNick(), posts);
            }
        }
    }

    private class Posts {

        List<Post> posts = new ArrayList<>();

        public boolean addPost(String lastPost) {
            posts.add(new Post(System.currentTimeMillis(), lastPost));
            if (posts.size() == MAX_MESSAGES) {
                boolean areSame = true;
                for (int i = 1; i < posts.size() && areSame; i++) {
                    if (!posts.get(i - 1).message.equalsIgnoreCase(posts.get(i).message)) {
                        areSame = false;
                    }
                }
                if (areSame) {
                    if (posts.get(posts.size() - 1).getTime() - posts.get(0).getTime() < DUPE_RATE) {
                        return true;
                    }
                }
                if (posts.get(posts.size() - 1).getTime() - posts.get(0).getTime() < SPAM_RATE) {
                    return true;
                }
                posts.remove(0);
            }
            return false;
        }
    }

    private class Post {

        long timePosted;
        String message;

        public Post(long Time, String Message) {
            timePosted = Time;
            message = Message;
        }

        public String getMessage() {
            return message;
        }

        public long getTime() {
            return timePosted;
        }
    }
}
