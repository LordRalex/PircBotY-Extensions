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
package net.ae97.aebot.threads;

import net.ae97.aebot.AeBot;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.exceptions.NickNotOnlineException;
import net.ae97.aebot.api.users.BotUser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jline.console.ConsoleReader;

/**
 *
 * @author Joshua
 */
public final class KeyboardListener extends Thread {

    final ConsoleReader kb;
    final AeBot instance;
    BotUser bot;

    public KeyboardListener(AeBot a) throws IOException, NickNotOnlineException {
        setName("Keyboard_Listener_Thread");
        kb = new ConsoleReader();
        instance = a;
    }

    @Override
    public void run() {
        bot = BotUser.getBotUser();
        String line;
        boolean run = true;
        String currentChan = "";
        try {
            while (run) {
                try {
                    line = kb.readLine();
                    if (line == null) {
                        run = false;
                    } else {
                        if (line.startsWith("$")) {
                            String cmd = line.substring(1).split(" ")[0];
                            if (cmd.equalsIgnoreCase("channel")) {
                                currentChan = line.split(" ")[1].toLowerCase();
                                System.out.println("Now talking in " + currentChan);
                            } else if (cmd.equalsIgnoreCase("stop")) {
                                run = false;
                            } else if (cmd.equalsIgnoreCase("join")) {
                                bot.joinChannel(line.split(" ")[1]);
                            } else if (cmd.equalsIgnoreCase("leave")) {
                                bot.leaveChannel(line.split(" ")[1]);
                            } else if (cmd.equalsIgnoreCase("me")) {
                                String action = line.substring(3).trim();
                                if (currentChan != null && !currentChan.isEmpty()) {
                                    bot.sendAction(currentChan, action);
                                }
                            } else if (cmd.equalsIgnoreCase("kick")) {
                                List<String> args = new ArrayList<>();
                                String[] parts = line.split(" ");
                                args.addAll(Arrays.asList(parts));
                                args.remove(0);
                                String chan;
                                if (args.get(0).startsWith("#")) {
                                    chan = args.remove(0);
                                } else {
                                    chan = currentChan;
                                }
                                String target = args.remove(0);
                                String reason = "";
                                if (args.size() > 0) {
                                    for (String part : args) {
                                        reason += part + " ";
                                    }
                                    reason = reason.trim();
                                } else {
                                    reason = bot.getNick() + " has kicked " + target + " from the channel";
                                }
                                if (bot.hasOP(chan)) {
                                    bot.kick(target, chan, reason);
                                } else {
                                    bot.sendMessage("chanserv", "kick " + chan + " " + target + " " + reason);
                                }
                            } else if (cmd.equalsIgnoreCase("reload")) {
                                instance.getEventHandler().fireEvent(new CommandEvent(null, null, "reload", new String[0]));
                            }
                        } else {
                            if (currentChan == null || currentChan.isEmpty()) {
                            } else {
                                bot.sendMessage(currentChan, line);
                            }
                        }
                    }
                } catch (IOException ex) {
                    AeBot.logSevere("An error occurred", ex);
                }
            }
        } catch (Exception e) {
        }
        synchronized (instance) {
            instance.notify();
        }
        kb.shutdown();
        AeBot.log("Ending keyboard listener");
    }

    public ConsoleReader getJLine() {
        return kb;
    }
}
