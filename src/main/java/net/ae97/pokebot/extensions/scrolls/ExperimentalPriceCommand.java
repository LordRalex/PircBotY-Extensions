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
public class ExperimentalPriceCommand implements CommandExecutor {

    private final String url;

    public ExperimentalPriceCommand() {
        url = "http://a.scrollsguide.com/experimentalprices?name={name}&days={days}";
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (event.getArgs().length == 0 || (event.getArgs()[0].equals("-d") && event.getArgs().length < 3)) {
            event.respond("Usage: .price <-d days> [name]");
            return;
        }
        int days = 2;
        String[] name = event.getArgs();
        if (event.getArgs()[0].equals("-d")) {
            name = new String[event.getArgs().length - 2];
            for (int i = 2; i < event.getArgs().length; i++) {
                name[i - 2] = event.getArgs()[i];
            }
            try {
                days = Integer.parseInt(event.getArgs()[1]);
            } catch (NumberFormatException e) {
                event.respond("Usage: .price <-d days> [name]");
                return;
            }
            if (days > 10 || days < 0) {
                event.respond("The days must be between 1 and 10 (inclusive)");
                return;
            }
        }
        try {
            URL playerURL = new URL(url.replace("{days}", Integer.toString(days)).replace("{name}", StringUtils.join(name, "%20")));
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

            builder.append("Buy: ").append(dataObject.get("buy").getAsJsonObject().get("price").getAsInt()).append(" Gold - ");
            builder.append("Bought: ").append(dataObject.get("buy").getAsJsonObject().get("pop").getAsInt()).append(" - ");
            builder.append("Sell: ").append(dataObject.get("sell").getAsJsonObject().get("price").getAsInt()).append(" Gold - ");
            builder.append("Sold: ").append(dataObject.get("sell").getAsJsonObject().get("pop").getAsInt());
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
        return new String[]{"priceexp"};
    }
}
