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
package net.ae97.pokebot.extensions.google;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lord_Ralex
 */
public class GoogleExtension extends Extension implements CommandExecutor {

    @Override
    public String getName() {
        return "Google";
    }

    @Override
    public void load() {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        final String[] args = event.getArgs();
        BufferedReader reader = null;
        String total = StringUtils.join(args, " ");
        if (args.length == 0 || total.isEmpty()) {
            event.respond("http://www.google.com");
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
                    event.respond(string);
                    break;
                }
            }
        } catch (IOException ex) {
            PokeBot.getLogger().log(Level.SEVERE, null, ex);
            event.respond("An error occureed");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                PokeBot.getLogger().log(Level.SEVERE, null, ex);
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
