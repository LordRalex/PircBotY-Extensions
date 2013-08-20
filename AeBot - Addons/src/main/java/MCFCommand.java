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
import net.ae97.aebot.api.Utilities;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.sender.Sender;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MCFCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String[] args = event.getArgs();
        BufferedReader reader = null;
        Sender target = event.getChannel();
        if (target == null) {
            target = event.getUser();
        }
        if (target == null) {
            return;
        }
        String total = Utilities.toString(args);
        if (args.length == 0 || total.isEmpty()) {
            target.sendMessage("http://www.minecraftforum.net");
            return;
        }
        try {
            String url = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=site:http://www.minecraftforum.net%20" + total.replace(" ", "%20");
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
            for (String string : b) {
                if (string.startsWith("\"url\":")) {
                    string = string.replace("\"", "");
                    string = string.replace("url:", "");
                    target.sendMessage(string);
                    break;
                }
            }
        } catch (IOException ex) {
            AeBot.logSevere(null, ex);
            target.sendMessage("An error occured");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                AeBot.logSevere(null, ex);
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "mcf"
        };
    }
}
