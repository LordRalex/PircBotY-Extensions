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

import net.ae97.ralexbot.RalexBot;
import net.ae97.ralexbot.api.EventField;
import net.ae97.ralexbot.api.EventType;
import net.ae97.ralexbot.api.Listener;
import net.ae97.ralexbot.api.Priority;
import net.ae97.ralexbot.api.channels.Channel;
import net.ae97.ralexbot.api.events.CommandEvent;
import net.ae97.ralexbot.api.sender.Sender;
import net.ae97.ralexbot.api.users.User;
import net.ae97.ralexbot.settings.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeadflyCommand extends Listener {

    private String adflyLine;

    @Override
    public void onLoad() {
        adflyLine = Settings.getGlobalSettings().getString("adfly");
        if (adflyLine == null || adflyLine.isEmpty()) {
            adflyLine = "var zzz";
        }
    }

    @Override
    @EventType(event = EventField.Command, priority = Priority.NORMAL)
    public void runEvent(CommandEvent event) {
        final User sender = event.getUser();
        final String[] args = event.getArgs();
        final Channel channel = event.getChannel();
        Sender target = channel;
        if (target == null) {
            target = sender;
        }
        if (target == null) {
            return;
        }
        if (args.length == 0) {
            target.sendMessage("*deadfly <link>");
            return;
        }
        String reply = "";
        BufferedReader reader = null, redirectReader = null;
        try {
            String url = args[0].replace(" ", "%20");
            URL path = new URL(url);
            reader = new BufferedReader(new InputStreamReader(path.openStream()));
            List<String> parts = new ArrayList<>();
            String s;
            while ((s = reader.readLine()) != null) {
                parts.add(s);
            }
            List<String> b = new ArrayList<>();
            for (String part : parts) {
                String[] c = part.split(",");
                b.addAll(Arrays.asList(c));
            }
            String forward = null;
            for (String string : b) {
                string = string.trim();
                if (string.startsWith("var zzz")) {
                    string = string.replace("var zzz =", "");
                    string = string.replace("\'", "");
                    string = string.replace(";", "");
                    string = string.trim();
                    forward = string;
                    break;
                }
            }
            reply = forward;
        } catch (IOException ex) {
            RalexBot.logSevere(null, ex);
            reply = "There was a problem handling the link";
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                RalexBot.logSevere(null, ex);
            }
            try {
                if (redirectReader != null) {
                    redirectReader.close();
                }
            } catch (IOException ex) {
                RalexBot.logSevere(null, ex);
            }
        }
        target.sendMessage(reply);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "deadfly",
            "df"
        };
    }
}
