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
package net.ae97.pokebot.extensions.antispam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.ae97.pircboty.Channel;
import net.ae97.pircboty.User;
import net.ae97.pircboty.api.events.ActionEvent;
import net.ae97.pircboty.api.events.MessageEvent;
import net.ae97.pircboty.generics.GenericChannelUserEvent;
import net.ae97.pircboty.generics.GenericMessageEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.EventExecutor;
import net.ae97.pokebot.api.Listener;
import net.ae97.pokebot.api.Priority;
import net.ae97.pokebot.extension.Extension;
import net.ae97.pokebot.extension.ExtensionReloadFailedException;

/**
 * @author Lord_Ralex
 */
public class AntiSpamExtension extends Extension implements Listener {

    private final Map<String, Posts> logs = new ConcurrentHashMap<>();
    private final CleanerTask cleaner = new CleanerTask();

    @Override
    public String getName() {
        return "Antispam";
    }

    @Override
    public void load() {
        logs.clear();
        PokeBot.getExtensionManager().addListener(this);
        PokeBot.getScheduler().scheduleTask(cleaner, 30, TimeUnit.MINUTES);
    }

    @Override
    public void reload() throws ExtensionReloadFailedException {
        super.reload();
        logs.clear();
    }

    @EventExecutor(priority = Priority.LOW)
    public void runEvent(MessageEvent event) {
        handle(event);
    }

    @EventExecutor(priority = Priority.LOW)
    public void runEvent(ActionEvent event) {
        handle(event);
    }

    private void handle(GenericChannelUserEvent event) {
        synchronized (logs) {
            Channel channel = event.getChannel();
            if (!getChannels().contains(channel.getName().toLowerCase())) {
                return;
            }
            User sender = event.getUser();
            if (channel.getOps().contains(sender) || channel.getVoices().contains(sender)) {
                return;
            }
            String message;
            if (event instanceof GenericMessageEvent) {
                message = ((GenericMessageEvent) event).getMessage();
            } else {
                return;
            }
            message = message.toLowerCase();
            Posts posts = logs.remove(sender.getNick());
            if (posts == null) {
                posts = new Posts(getConfig().getInt("message-count"), getConfig().getInt("dupe-rate"), getConfig().getInt("spam-rate"));
            }
            if (posts.addPost(message, event.getTimestamp())) {
                event.getChannel().send().kick(sender, getConfig().getString("kickmessage").replace("{ip}", sender.getHostmask()));
            } else {
                logs.put(sender.getNick(), posts);
            }
        }
    }

    private List<String> getChannels() {
        return getConfig().getStringList("channels");
    }

    private class CleanerTask implements Runnable {

        private final int DELAY = 10;

        @Override
        public void run() {
            synchronized (logs) {
                List<String> keys = new ArrayList<>(logs.keySet());
                for (String key : keys) {
                    Posts log = logs.get(key);
                    log.cleanLog();
                    if (log.isEmpty()) {
                        logs.remove(key);
                    }
                }
            }
            PokeBot.getScheduler().scheduleTask(CleanerTask.this, DELAY, TimeUnit.MINUTES);
        }
    }
}
