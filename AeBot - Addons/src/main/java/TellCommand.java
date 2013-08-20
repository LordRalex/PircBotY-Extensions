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

import net.ae97.aebot.AeBot;
import net.ae97.aebot.api.EventField;
import net.ae97.aebot.api.EventType;
import net.ae97.aebot.api.Listener;
import net.ae97.aebot.api.channels.Channel;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.events.JoinEvent;
import net.ae97.aebot.api.events.MessageEvent;
import net.ae97.aebot.api.events.NickChangeEvent;
import net.ae97.aebot.api.users.User;
import net.ae97.aebot.settings.Settings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TellCommand extends Listener {

    private Map<String, Long> lastTold = new ConcurrentHashMap<>();
    private int refresh;

    @Override
    public void onLoad() {
        refresh = Settings.getGlobalSettings().getInt("refresh-minutes");
        if (refresh == 0) {
            refresh = 5;
        }
    }

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        User sender = event.getUser();

        String[] tells;
        try {
            tells = getTells(sender.getNick());
        } catch (FileNotFoundException ex) {
            return;
        }
        if (tells.length > 0) {
            Long timeAgo = lastTold.get(event.getUser().getNick());
            if (timeAgo == null || System.currentTimeMillis() - timeAgo.longValue() > refresh * 1000 * 60) {
                lastTold.put(event.getUser().getNick(), System.currentTimeMillis());
                sender.sendMessage("You have messages waiting for you, using ``st will show you them");
            }
        }
    }

    @Override
    @EventType(event = EventField.NickChange)
    public void runEvent(NickChangeEvent event) {
        String sender = event.getNewNick();
        String[] tells;
        try {
            tells = getTells(sender);
        } catch (FileNotFoundException ex) {
            return;
        }
        Long timeAgo = lastTold.get(event.getOldNick());
        if (timeAgo != null) {
            lastTold.put(event.getNewNick(), timeAgo);
        }
        if (tells.length > 0) {
            if (timeAgo == null || System.currentTimeMillis() - timeAgo.longValue() > refresh * 1000 * 60) {
                lastTold.put(event.getNewNick(), System.currentTimeMillis());
                User.getUser(sender).sendMessage("You have messages waiting for you, using ``st will show you them");
            }
        }
    }

    @Override
    @EventType(event = EventField.Message)
    public void runEvent(MessageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        User sender = event.getUser();
        String[] tells;
        try {
            tells = getTells(sender.getNick());
        } catch (FileNotFoundException ex) {
            return;
        }
        if (tells.length > 0) {
            Long timeAgo = lastTold.get(event.getUser().getNick());
            if (timeAgo == null || System.currentTimeMillis() - timeAgo.longValue() > refresh * 1000 * 60) {
                lastTold.put(event.getUser().getNick(), System.currentTimeMillis());
                sender.sendMessage("You have messages waiting for you, using ``st will show you them");
            }
        }
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        Channel channel = event.getChannel();
        User sender = event.getUser();
        String[] args = event.getArgs();
        String command = event.getCommand();
        if (sender == null || sender.getNick().isEmpty()) {
            return;
        }
        if (command.equalsIgnoreCase("t") || command.equalsIgnoreCase("tell")) {
            String message = "";
            for (int i = 1; i < args.length; i++) {
                message += args[i] + " ";
            }
            message = message.trim();
            String target = args[0];
            try {
                addTell(sender.getNick(), target, message);
            } catch (IOException ex) {
                Logger.getLogger(TellCommand.class.getName()).log(Level.SEVERE, null, ex);
                if (channel != null) {
                    channel.sendMessage("An error occurred, get Lord_Ralex to see what went wrong");
                } else if (sender != null) {
                    sender.sendMessage("An error occurred, get Lord_Ralex to see what went wrong");
                }
                return;
            }
            if (channel != null) {
                channel.sendMessage("Message will be delivered to " + target);
            } else if (sender != null) {
                sender.sendMessage("Message will be delivered to " + target);
            }
        } else if (command.equalsIgnoreCase("st") || command.equalsIgnoreCase("showtells")) {
            if (sender == null) {
                return;
            }
            String[] messages = null;
            try {
                messages = getTells(sender.getNick());
            } catch (FileNotFoundException ex) {
                messages = new String[0];
            }
            if (messages == null || messages.length == 0) {
                sender.sendMessage("I have no messages for you");
            } else {
                sender.sendMessage(messages);
            }
            clearTells(sender.getNick());
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "tell",
            "t",
            "showtells",
            "st"
        };
    }

    public void saveTells(String name, String[] lines) {
        if (name == null || lines == null || name.length() == 0) {
            return;
        }
        name = name.toLowerCase();
        new File("data", "tells").mkdirs();
        new File(new File("data", "tells"), name + ".txt").delete();
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(new File("data", "tells"), name + ".txt"));
            for (String line : lines) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    AeBot.logSevere(null, ex);
                }
            }
        }
    }

    public void clearTells(String name) {
        saveTells(name, new String[0]);
    }

    public String[] getTells(String name) throws FileNotFoundException {
        if (name == null || name.length() == 0) {
            return new String[0];
        }
        name = name.toLowerCase();
        List<String> lines = new ArrayList<>();
        Scanner reader = new Scanner(new File(new File("data", "tells"), name + ".txt"));
        while (reader.hasNext()) {
            lines.add(reader.nextLine().trim());
        }
        String[] result = lines.toArray(new String[0]);
        return result;
    }

    public void addTell(String sender, String target, String message) throws IOException {
        if (target == null || sender == null || message == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        String[] old;
        try {
            old = getTells(target);
        } catch (FileNotFoundException ex) {
            old = new String[0];
        }
        lines.addAll(Arrays.asList(old));
        lines.add("From " + sender + "-> " + message);
        saveTells(target, lines.toArray(new String[0]));
    }
}
