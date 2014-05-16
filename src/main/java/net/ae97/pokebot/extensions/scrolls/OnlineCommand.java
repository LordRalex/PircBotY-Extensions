/*
 * Copyright (C) 2014 Lord_Ralex
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
package net.ae97.pokebot.extensions.scrolls;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lord_Ralex
 */
public class OnlineCommand implements CommandExecutor {

    private final URL onlineURL;

    public OnlineCommand() throws MalformedURLException {
        onlineURL = new URL("http://a.scrollsguide.com/online");
    }

    @Override
    public void runEvent(CommandEvent event) {
        try {
            List<String> lines = new LinkedList<>();
            HttpURLConnection conn = (HttpURLConnection) onlineURL.openConnection();
            conn.setRequestProperty("User-Agent", "PokeBot - " + PokeBot.VERSION);
            conn.connect();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(StringUtils.join(lines, "\n"));
            JsonObject obj = element.getAsJsonObject();
            int online = obj.get("data").getAsJsonObject().get("online").getAsInt();
            event.respond("there are " + online + " online users in Scrolls");
        } catch (IOException | JsonSyntaxException ex) {
            PokeBot.getLogger().log(Level.SEVERE, "Error on getting online players for Scrolls", ex);
            event.respond("error on finding online players: " + ex.getLocalizedMessage());
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"online"};
    }
}
