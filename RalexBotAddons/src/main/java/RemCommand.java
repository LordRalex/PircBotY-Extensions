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

import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.Utilities;
import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.users.User;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class RemCommand extends Listener {

    Map<String, String> remMap = new ConcurrentHashMap<>();
    List<String> dontReply = new ArrayList<>();

    @Override
    public void onLoad() {
        remMap.clear();
        new File("data" + File.separator + "rem").mkdirs();
        for (File file : new File("data" + File.separator + "rem").listFiles()) {
            try {
                String name = file.getName().substring(0, file.getName().length() - 4).toLowerCase().trim();
                Scanner reader = new Scanner(file);
                String line = reader.nextLine().trim();
                remMap.put(name, line);
            } catch (FileNotFoundException ex) {
                RalexBot.logSevere(null, ex);
            }
        }
    }

    @Override
    public void onUnload() {
        remMap.clear();
    }

    @Override
    @EventType(event = EventField.Command, priority = Priority.HIGH)
    public void runEvent(CommandEvent event) {
        String command = event.getCommand().toLowerCase();
        String[] args = event.getArgs();

        User user = event.getSender();
        Channel channel = event.getChannel();

        if (command.equalsIgnoreCase("remshutup")) {
            if (!user.hasOP(channel.getName())) {
                return;
            }
            String target = channel.getName();
            if (args.length != 0) {
                target = args[0];
            }
            target = target.toLowerCase();
            boolean wasThere = dontReply.remove(channel.getName());
            if (wasThere) {
                if (channel != null) {
                    channel.sendMessage("Returning to normal");
                } else {
                    user.sendMessage("Returning to normal");
                }
            } else {
                dontReply.add(target);
                if (channel != null) {
                    channel.sendMessage("Shutting up");
                } else {
                    user.sendMessage("Shutting up");
                }
            }
            return;
        }

        if (dontReply.contains(channel.getName())) {
            return;
        }

        if (command.equalsIgnoreCase("remupdate")) {
            onUnload();
            onLoad();
            user.sendMessage("Rems updated");
            return;
        }

        if (isRem(command)) {
            String reply = remMap.get(command);
            if (user == null) {
                return;
            }
            Map<String, String> placers = new HashMap<>();
            placers.put("User", user.getNick());
            placers.put("Channel", channel.getName());
            for (int i = 0; i < args.length; i++) {
                placers.put(new Integer(i).toString(), args[i]);
            }
            reply = Utilities.handleArgs(reply, placers);
            String[] entire = reply.split("\n");
            if (channel != null) {
                channel.sendMessage(entire);
            } else {
                user.sendMessage(entire);
            }
            return;
        }

        if (args.length == 0) {
            return;
        }

        String[] part = buildRem(args);
        String name = part[0].toLowerCase().trim();
        String reply = part[1];

        if (reply == null || reply.equalsIgnoreCase("null") || reply.equalsIgnoreCase("forget")) {
            remMap.remove(name);
            deleteRem(name);
            if (channel != null) {
                channel.sendMessage(name + " is now forgotten");
            } else {
                user.sendMessage(name + " is now forgotten");
            }
            return;
        }

        if (remMap.containsKey(name)) {
            if (channel != null) {
                channel.sendMessage(name + " is already known");
            } else {
                user.sendMessage(name + " is already known");
            }
            return;
        }

        remMap.put(name, reply);
        saveRem(name, reply);
        if (channel != null) {
            channel.sendMessage(name + " is now known");
        } else {
            user.sendMessage(name + " is now known");
        }
    }

    private String[] buildRem(String[] args) {
        String[] complete = new String[2];
        complete[0] = args[0];
        complete[1] = "";
        for (int i = 1; i < args.length; i++) {
            complete[1] += args[i] + " ";
        }
        complete[1] = complete[1].trim();
        if (complete[1].equalsIgnoreCase("")) {
            complete[1] = null;
        }
        return complete;
    }

    private boolean isRem(String command) {
        return remMap.containsKey(command.toLowerCase().trim());
    }

    @Override
    public String[] getAliases() {
        List<String> list = new ArrayList<>();
        list.add("rem");
        list.add("r");
        list.add("remember");
        list.add("remshutup");
        list.add("remupdate");
        for (Object command : remMap.keySet()) {
            list.add(((String) command).toLowerCase());
        }
        return list.toArray(new String[0]);
    }

    public void saveRem(String name, String line) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(new File("data", "rem"), name + ".txt"));
            writer.write(line);
            writer.flush();
        } catch (IOException ex) {
            RalexBot.logSevere(null, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    RalexBot.logSevere(null, ex);
                }
            }
        }
    }

    public void deleteRem(String name) {
        new File(new File("data", "rem"), name + ".txt").delete();
    }
}
