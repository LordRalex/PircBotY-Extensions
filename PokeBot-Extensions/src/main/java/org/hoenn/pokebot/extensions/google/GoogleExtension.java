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
package org.hoenn.pokebot.extensions.google;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.CommandExecutor;
import org.hoenn.pokebot.api.Utilities;
import org.hoenn.pokebot.api.events.CommandEvent;
import org.hoenn.pokebot.api.recipients.MessageRecipient;
import org.hoenn.pokebot.extension.Extension;

/**
 * @author Lord_Ralex
 */
public class GoogleExtension extends Extension implements CommandExecutor {

    @Override
    public void load() {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final String[] args = event.getArgs();
        BufferedReader reader = null;
        MessageRecipient target = event.getChannel();
        if (target == null) {
            target = event.getUser();
        }
        if (target == null) {
            return;
        }
        String total = Utilities.toString(args);
        if (args.length == 0 || total.isEmpty()) {
            target.sendMessage("http://www.google.com");
            return;
        }
        try {
            String url = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=" + total.replace(" ", "%20");
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
            PokeBot.log(Level.SEVERE, null, ex);
            target.sendMessage("An error occureed");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                PokeBot.log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "g",
            "google"
        };
    }

}
