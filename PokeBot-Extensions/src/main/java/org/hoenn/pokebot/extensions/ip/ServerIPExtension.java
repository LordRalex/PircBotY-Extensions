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
package org.hoenn.pokebot.extensions.ip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.CommandExecutor;
import org.hoenn.pokebot.api.EventExecutor;
import org.hoenn.pokebot.api.Listener;
import org.hoenn.pokebot.api.Priority;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.events.ActionEvent;
import org.hoenn.pokebot.api.events.CommandEvent;
import org.hoenn.pokebot.api.events.MessageEvent;
import org.hoenn.pokebot.api.events.PartEvent;
import org.hoenn.pokebot.api.events.QuitEvent;
import org.hoenn.pokebot.api.users.BotUser;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.extension.Extension;
import org.hoenn.pokebot.settings.Settings;

/**
 * @author Lord_Ralex
 */
public class ServerIPExtension extends Extension implements CommandExecutor, Listener {

    private final List<String> triggered = new ArrayList();
    private final List<String> ignorePeople = new ArrayList();
    private final Set<String> channels = new HashSet<>();

    @Override
    public void load() {
        try {
            Settings settings = new Settings();
            settings.load(new File("configs", "iplistener.yml"));
            channels.addAll(settings.getStringList("channels"));
        } catch (IOException ex) {
            PokeBot.log(Level.SEVERE, "Error loading settings file, disabling", ex);
            return;
        }
        PokeBot.getInstance().getExtensionManager().addCommandExecutor(this);
        PokeBot.getInstance().getExtensionManager().addListener(this);
    }

    @EventExecutor(priority = Priority.HIGH)
    public void runEvent(MessageEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        String message = event.getMessage();
        User sender = event.getUser();
        Channel channel = event.getChannel();
        boolean silence = false;
        if (ignorePeople.contains(sender.getNick().toLowerCase()) || ignorePeople.contains(channel.getName().toLowerCase())) {
            return;
        }

        if (triggered.contains(sender.getNick().toLowerCase())) {
            silence = true;
        } else if (triggered.contains(event.getHostname().toLowerCase())) {
            silence = true;
        }
        String[] messageParts = message.split(" ");
        for (String part : messageParts) {
            if (isServer(part)) {
                if (!silence) {
                    channel.sendMessage(sender.getNick() + ", please do not advertise servers here");
                    triggered.remove(sender.getNick().toLowerCase());
                    triggered.remove(event.getHostname().toLowerCase());
                    triggered.add(sender.getNick().toLowerCase());
                    triggered.add(event.getHostname().toLowerCase());
                } else if (triggered.contains(sender.getNick().toLowerCase())) {
                    BotUser.getBotUser().kick(sender.getNick(), channel.getName(), "Server advertisement");
                    event.setCancelled(true);
                }
                break;
            }
        }
    }

    @EventExecutor
    public void runEvent(ActionEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        String message = event.getAction();
        User sender = event.getUser();
        Channel channel = event.getChannel();
        boolean silence = false;
        if (ignorePeople.contains(sender.getNick().toLowerCase()) || ignorePeople.contains(channel.getName().toLowerCase())) {
            return;
        }

        if (triggered.contains(sender.getNick().toLowerCase())) {
            silence = true;
        } else if (triggered.contains(event.getUser().getIP().toLowerCase())) {
            silence = true;
        }
        String[] messageParts = message.split(" ");
        for (String part : messageParts) {
            if (isServer(part)) {
                if (!silence) {
                    channel.sendMessage(sender.getNick() + ", please do not advertise servers here");
                    triggered.remove(event.getUser().getIP().toLowerCase());
                    triggered.remove(event.getUser().getIP().toLowerCase());
                    triggered.add(sender.getNick().toLowerCase());
                    triggered.add(event.getUser().getIP().toLowerCase());
                } else if (triggered.contains(sender.getNick().toLowerCase())) {
                    BotUser.getBotUser().kick(sender.getNick(), channel.getName(), "Server advertisement");
                    event.setCancelled(true);
                }
                break;
            }
        }
    }

    @EventExecutor
    public void runEvent(PartEvent event) {
        //triggered.remove(event.getUser().getNick().toLowerCase());
    }

    @EventExecutor
    public void runEvent(QuitEvent event) {
        //triggered.remove(event.getUser().getNick().toLowerCase());
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (!event.getUser().hasOP(event.getChannel().getName())) {
            return;
        }
        if (event.getCommand().equalsIgnoreCase("ignoread")) {
            if (event.getArgs().length == 1) {
                ignorePeople.add(event.getArgs()[0].toLowerCase());
                event.getChannel().sendMessage("He will be ignored with IPs now");

            }
        } else if (event.getCommand().equalsIgnoreCase("unignoread")) {
            if (event.getArgs().length == 1) {
                if (ignorePeople.remove(event.getArgs()[0].toLowerCase())) {
                    event.getChannel().sendMessage("He will be not ignored with IPs now");
                }

            }
        } else if (event.getCommand().equalsIgnoreCase("reset")) {
            if (event.getArgs().length == 1) {
                if (triggered.remove(event.getArgs()[0].toLowerCase())) {
                    event.getChannel().sendMessage("His counter was removed");
                }

            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "reset",
            "unignoread",
            "ignoread"
        };
    }

    private boolean isServer(String testString) {
        String test = testString.toLowerCase().trim();

        String[] parts = split(test, ".");
        if (parts.length == 4) {
            if (parts[3].contains(":")) {
                parts[3] = parts[3].split(":")[0];
            }
            for (int i = 0; i < 4; i++) {
                try {
                    Integer.parseInt(parts[i]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private String[] split(String message, String lookFor) {
        List<String> parts = new ArrayList<>();
        String test = message.toString();
        while (test.contains(lookFor)) {
            int id = test.indexOf(lookFor);
            if (id == -1) {
                break;
            }
            parts.add(test.substring(0, id));
            test = test.substring(id + 1);
        }
        parts.add(test);

        return parts.toArray(new String[0]);
    }
}
