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
public class PlayerCommand implements CommandExecutor {

    private final String url;
    private final BadgeRanks badges;
    private final ScrollsExtension extension;

    public PlayerCommand(ScrollsExtension extension) {
        this.extension = extension;
        url = "http://a.scrollsguide.com/player?name={name}&fields=all";
        badges = new BadgeRanks(this.extension);
    }

    @Override
    public void runEvent(CommandEvent event) {
        String name = event.getArgs().length > 0 ? event.getArgs()[0] : event.getUser().getNick();
        try {
            URL playerURL = new URL(url.replace("{name}", name));
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
                event.respond("No data could be retrieved for " + name);
                return;
            }

            JsonObject dataObject = obj.get("data").getAsJsonObject();

            StringBuilder builder = new StringBuilder();

            if (event.getUser().getNick().equalsIgnoreCase(dataObject.get("name").getAsString())) {
                builder.append("Your");
            } else {
                builder.append(dataObject.get("name").getAsString()).append("'s");
            }
            builder.append(" stats - ");
            builder.append("Rating: ").append(dataObject.get("rating").getAsInt()).append(" - ");
            builder.append("Rank: ").append(dataObject.get("rank").getAsInt()).append(" - ");
            builder.append("Badge: ").append(badges.getBadge(dataObject.get("badgerank").getAsString())).append(" - ");
            builder.append("Played: ").append(dataObject.get("played").getAsInt()).append(" - ");
            builder.append("Won: ").append(dataObject.get("won").getAsInt());
            builder.append(" (");
            builder.append((int) ((dataObject.get("won").getAsDouble() / dataObject.get("played").getAsDouble()) * 100));
            builder.append("%) - ");
            builder.append("Judgement wins: ").append(dataObject.get("limitedwon").getAsInt()).append(" - ");
            builder.append("Ranked wins: ").append(dataObject.get("rankedwon").getAsInt()).append(" - ");
            builder.append("Last game played: ").append(parseTime(dataObject.get("lastgame").getAsInt()));

            event.respond(builder.toString());
        } catch (IOException | JsonSyntaxException ex) {
            extension.getLogger().log(Level.SEVERE, "Error on getting player stats for Scrolls for " + name, ex);
            event.respond("Error on getting player stats: " + ex.getLocalizedMessage());
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"player"};
    }

    private String parseTime(int time) {
        if (time < 0) {
            return "Never played";
        }
        if (time < 10) {
            return "Just now";
        }
        String[] strs = new String[]{"second", "minute", "hour", "day", "week", "month"};

        int[] duration = new int[]{1, 60, 3600, 86400, 604800, 2630880};
        double no = 0;

        int i;
        for (i = duration.length - 1; (i >= 0) && ((no = time / duration[i]) < 1); i--) {
        }

        int t = (int) Math.floor(no);
        return t + " " + strs[i] + ((t > 1) ? "s" : "") + " ago";
    }
}
