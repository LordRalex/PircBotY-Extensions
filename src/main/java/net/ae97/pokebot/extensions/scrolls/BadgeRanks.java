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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.ae97.pokebot.PokeBot;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Lord_Ralex
 */
public class BadgeRanks {

    private long lastUpdate;
    private final Map<String, String> ranks = new HashMap<>();
    private final String syncURL = "http://a.scrollsguide.com/ranks";
    private final ScrollsExtension extension;

    public BadgeRanks(ScrollsExtension extension) {
        this.extension = extension;
        lastUpdate = -1;
    }

    private synchronized void sync() {
        if (lastUpdate + (1000 * 60 * 60 * 24) > System.currentTimeMillis()) {
            return;
        }
        try {
            URL url = new URL(syncURL);
            List<String> lines = new LinkedList<>();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                throw new IOException("API replied with error");
            }

            JsonArray dataObject = obj.get("data").getAsJsonArray();
            synchronized (ranks) {
                ranks.clear();
                for (int i = 0; i < dataObject.size(); i++) {
                    JsonObject rank = dataObject.get(i).getAsJsonObject();
                    ranks.put(rank.get("id").getAsString(), rank.get("name").getAsString());
                }
                ranks.put("-1", "No badge");
            }
            lastUpdate = System.currentTimeMillis();
        } catch (IOException | JsonSyntaxException ex) {
            extension.getLogger().log(Level.SEVERE, "Error on syncing badge ranks", ex);
            lastUpdate = -1;
        }
    }

    public synchronized String getBadge(String rank) {
        sync();
        return ranks.get(rank);
    }

}
