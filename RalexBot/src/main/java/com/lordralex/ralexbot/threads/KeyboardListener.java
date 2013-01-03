/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.threads;

import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.Utils;
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

    ConsoleReader kb;
    final RalexBot instance;

    public KeyboardListener(RalexBot a) throws IOException {
        setName("Keyboard_Listener_Thread");
        kb = new ConsoleReader();
        instance = a;
    }

    @Override
    public void run() {
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
                                Utils.joinChannel(line.split(" ")[1]);
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
                                    reason = Utils.getNick() + " has kicked " + target + " from the channel";
                                }
                                if (Utils.hasOP(Utils.getNick(), chan)) {
                                    Utils.kick(target, chan, reason);
                                } else {
                                    Utils.sendMessage("chanserv", "kick " + chan + " " + target + " " + reason);
                                }
                            }
                        } else {
                            if (currentChan == null || currentChan.isEmpty()) {
                            } else {
                                Utils.sendMessage(currentChan, line);
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        } catch (Exception e) {
        }
        synchronized (instance) {
            instance.notify();
        }
        kb.shutdown();
        System.out.println("Ending keyboard listener");
    }
    
    public ConsoleReader getJLine() {
        return kb;
    }
}
