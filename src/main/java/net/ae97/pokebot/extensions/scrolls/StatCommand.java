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
public class StatCommand implements CommandExecutor {

    private final String url = "http://a.scrollsguide.com/statistics";

    @Override
    public void runEvent(CommandEvent event) {
        try {
            List<String> lines = new LinkedList<>();
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
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
            JsonObject dataObject = obj.get("data").getAsJsonObject();

            StringBuilder builder = new StringBuilder();
            builder.append("Stats - ");
            builder.append("Online today: ").append(dataObject.get("onlinetoday").getAsInt());
            builder.append(" - ");
            builder.append("Gold earned: ").append(dataObject.get("goldearned").getAsInt());
            builder.append(" - ");
            builder.append("Games played: ").append(dataObject.get("gamesplayed").getAsInt());
            builder.append(" - ");
            builder.append("Total users: ").append(dataObject.get("totalusers").getAsInt());
            String message = builder.toString();
            event.respond(message);
        } catch (IOException | JsonSyntaxException ex) {
            PokeBot.getLogger().log(Level.SEVERE, "Error on getting stats for Scrolls", ex);
            event.respond("Error on getting stats: " + ex.getLocalizedMessage());
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"stats"};
    }
}
