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
package net.ae97.pokebot.extensions.faq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.ae97.pircboty.Channel;
import net.ae97.pircboty.ChatFormat;
import net.ae97.pircboty.User;
import net.ae97.pokebot.PokeBot;

/**
 * @author Lord_Ralex
 */
public class MessageTask implements Runnable {

    private final List<String> lines;
    private final Channel channel;
    private final User user;
    private final boolean notice;
    private final String name;
    private final String[] args;
    private final String messageFormat;
    private final int delay;

    public MessageTask(String na, String u, Channel c, String[] l, boolean n, String format, int d) {
        this(na, u, c, l, n, new String[0], format, d);
    }

    public MessageTask(String na, String u, Channel c, String[] l, boolean n, String[] a, String format, int d) {
        lines = new ArrayList<>(Arrays.asList(l));
        channel = c;
        if (u == null) {
            user = null;
        } else {
            user = PokeBot.getUser(u);
        }
        notice = n;
        name = na;
        args = a;
        messageFormat = format;
        delay = d;
    }

    @Override
    public void run() {
        try {
            if (lines.isEmpty()) {
                return;
            }
            String message = lines.remove(0);
            message = messageFormat.replace("{message}", message)
                    .replace("{target}", user == null ? "" : user.getNick())
                    .replace("{channel}", channel.getName())
                    .replace("{factoid}", name.toLowerCase())
                    .replace("{botname}", PokeBot.getBot().getNick());
            for (int i = 0; i < args.length; i++) {
                message = message.replace("{" + i + "}", args[i]);
            }
            for (ChatFormat color : ChatFormat.values()) {
                message = message.replace("{" + color.name().toLowerCase() + "}", color.toString());
            }
            message = message.replace("[b]", ChatFormat.BOLD.toString());
            message = message.replace("[u]", ChatFormat.UNDERLINE.toString());
            message = message.replace("[/b]", ChatFormat.NORMAL.toString());
            message = message.replace("[/u]", ChatFormat.NORMAL.toString());
            if (notice) {
                user.send().notice(message);
            } else {
                channel.send().message(message);
            }
            if (!lines.isEmpty()) {
                PokeBot.getScheduler().scheduleTask(this, delay, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            PokeBot.getLogger().log(Level.SEVERE, "Damn it", e);
        }
    }

    public void start() {
        PokeBot.getScheduler().scheduleTask(this, delay, TimeUnit.MILLISECONDS);
    }
}
