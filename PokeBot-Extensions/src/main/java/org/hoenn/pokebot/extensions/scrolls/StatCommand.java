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
package org.hoenn.pokebot.extensions.scrolls;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.CommandExecutor;
import org.hoenn.pokebot.api.events.CommandEvent;

/**
 * @author Lord_Ralex
 */
public class StatCommand implements CommandExecutor {

    private final URL url;

    public StatCommand() throws MalformedURLException {
        url = new URL("http://a.scrollsguide.com/statistics");
    }

    @Override
    public void runEvent(CommandEvent event) {
        try {
            List<String> lines = new LinkedList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
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
            if (event.getChannel() == null) {
                event.getUser().sendMessage(message);
            } else {
                event.getChannel().sendMessage(event.getUser().getNick() + ", " + Character.toLowerCase(message.charAt(0)) + message.substring(1));
            }
        } catch (IOException | JsonSyntaxException ex) {
            PokeBot.log(Level.SEVERE, "Error on getting stats for Scrolls", ex);
            if (event.getChannel() == null) {
                event.getUser().sendMessage("Error on getting stats: " + ex.getLocalizedMessage());
            } else {
                event.getChannel().sendMessage(event.getUser().getNick() + ", error on getting stats: " + ex.getLocalizedMessage());
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"stats"};
    }
}
