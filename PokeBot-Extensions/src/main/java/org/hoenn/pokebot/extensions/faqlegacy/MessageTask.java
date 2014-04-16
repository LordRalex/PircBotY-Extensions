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
package org.hoenn.pokebot.extensions.faqlegacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.users.User;
import org.pircbotx.Colors;

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
    private final Map<String, String> colors = new HashMap<>();

    public MessageTask(String na, String u, String c, String[] l, boolean n, String format, int d) {
        this(na, u, c, l, n, new String[0], format, d);
    }

    public MessageTask(String na, String u, String c, String[] l, boolean n, String[] a, String format, int d) {
        lines = new ArrayList<>(Arrays.asList(l));
        channel = PokeBot.getChannel(c);
        user = PokeBot.getUser(u);
        notice = n;
        name = na;
        args = a;
        messageFormat = format;
        delay = d;
        colors.put("black", Colors.BLACK);
        colors.put("blue", Colors.BLUE);
        colors.put("bold", Colors.BOLD);
        colors.put("brown", Colors.BROWN);
        colors.put("cyan", Colors.CYAN);
        colors.put("darkblue", Colors.DARK_BLUE);
        colors.put("darkgray", Colors.DARK_GRAY);
        colors.put("darkgrey", Colors.DARK_GRAY);
        colors.put("darkgreen", Colors.DARK_GREEN);
        colors.put("green", Colors.GREEN);
        colors.put("lightgray", Colors.LIGHT_GRAY);
        colors.put("lightgrey", Colors.LIGHT_GRAY);
        colors.put("magenta", Colors.MAGENTA);
        colors.put("normal", Colors.NORMAL);
        colors.put("reset", Colors.NORMAL);
        colors.put("olive", Colors.OLIVE);
        colors.put("purple", Colors.PURPLE);
        colors.put("red", Colors.RED);
        colors.put("reverse", Colors.REVERSE);
        colors.put("teal", Colors.TEAL);
        colors.put("underline", Colors.UNDERLINE);
        colors.put("white", Colors.WHITE);
        colors.put("yellow", Colors.YELLOW);
    }

    @Override
    public void run() {
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
        for (Map.Entry<String, String> color : colors.entrySet()) {
            message = message.replace("{" + color.getKey() + "}", color.getValue());
        }
        if (notice) {
            user.sendNotice(message);
        } else {
            channel.sendMessage(message);
        }
        if (!lines.isEmpty()) {
            PokeBot.getScheduler().scheduleTask(this, delay, TimeUnit.MILLISECONDS);
        }
    }

    public void start() {
        PokeBot.getScheduler().scheduleTask(this, delay, TimeUnit.MILLISECONDS);
    }
}
