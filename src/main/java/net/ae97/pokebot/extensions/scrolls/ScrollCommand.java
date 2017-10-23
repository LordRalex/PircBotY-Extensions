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
public class ScrollCommand implements CommandExecutor {

    private final String url = "http://a.scrollsguide.com/scrolls?name={name}&norules";

    @Override
    public void runEvent(CommandEvent event) {
        if (event.getArgs().length == 0) {
            event.respond("Usage: .scroll [name]");
            return;
        }
        try {
            URL playerURL = new URL(url.replace("{name}", StringUtils.join(event.getArgs(), "%20")));
            List<String> lines = new LinkedList<>();
            HttpURLConnection conn = (HttpURLConnection) playerURL.openConnection();
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

            String result = obj.get("msg").getAsString();
            if (!result.equalsIgnoreCase("success")) {
                event.respond("Scroll not found");
                return;
            }
            JsonObject dataObject = obj.get("data").getAsJsonArray().get(0).getAsJsonObject();

            StringBuilder builder = new StringBuilder();

            builder.append(dataObject.get("name").getAsString()).append(" - ");
            builder.append("Cost: ");
            if (dataObject.get("costgrowth").getAsInt() > 0) {
                builder.append(dataObject.get("costgrowth").getAsInt()).append(" Growth");
            } else if (dataObject.get("costorder").getAsInt() > 0) {
                builder.append(dataObject.get("costorder").getAsInt()).append(" Order");
            } else if (dataObject.get("costenergy").getAsInt() > 0) {
                builder.append(dataObject.get("costenergy").getAsInt()).append(" Energy");
            } else if (dataObject.get("costdecay").getAsInt() > 0) {
                builder.append(dataObject.get("costdecay").getAsInt()).append(" Decay");
            } else {
                builder.append("0");
            }
            builder.append(" - ");
            String kind = dataObject.get("kind").getAsString();
            kind = Character.toUpperCase(kind.charAt(0)) + kind.substring(1).toLowerCase();
            builder.append("Kind: ").append(kind).append(" - ");
            Rarity rarity = Rarity.get(dataObject.get("rarity").getAsInt());
            String rarityString = rarity.name().toLowerCase();
            rarityString = Character.toUpperCase(rarityString.charAt(0)) + rarityString.substring(1);
            builder.append("Rarity: ").append(rarityString);

            if (kind.equalsIgnoreCase("creature") || kind.equalsIgnoreCase("structure")) {
                builder.append(" - ");
                builder.append("Types: ").append(dataObject.get("types").getAsString()).append(" - ");
                builder.append("Attack: ").append(dataObject.get("ap").getAsInt()).append(" - ");
                builder.append("Cooldown: ").append(dataObject.get("ac").getAsInt()).append(" - ");
                builder.append("Health: ").append(dataObject.get("hp").getAsInt());
            }

            builder.append("\n");
            builder.append("Description: '").append(dataObject.get("description").getAsString()).append("' - ");
            builder.append("Flavor: '").append(dataObject.get("flavor").getAsString()).append("'");

            String[] message = builder.toString().split("\n");
            for (String msg : message) {
                event.respond("" + msg);
            }

        } catch (IOException | JsonSyntaxException | IllegalStateException ex) {
            PokeBot.getLogger().log(Level.SEVERE, "Error on getting scroll for Scrolls for '" + StringUtils.join(event.getArgs(), " ") + "'", ex);
            event.respond("Error on getting scroll: " + ex.getLocalizedMessage());
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"scroll"};
    }

    private enum Rarity {

        COMMON(0),
        UNCOMMON(1),
        RARE(2),
        UNKNOWN(-1);
        private final int rank;

        private Rarity(int r) {
            rank = r;
        }

        public static Rarity get(int i) {
            for (Rarity rarity : Rarity.values()) {
                if (rarity.rank == i) {
                    return rarity;
                }
            }
            return UNKNOWN;
        }
    }
}
