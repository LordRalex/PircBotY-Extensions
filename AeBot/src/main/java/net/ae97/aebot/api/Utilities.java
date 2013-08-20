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
package net.ae97.aebot.api;

import net.ae97.aebot.api.exceptions.IllegalActionException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.pircbotx.PircBotX;

/**
 *
 * @author Joshua
 */
public class Utilities {

    protected static PircBotX bot;

    protected Utilities() {
    }

    public static void setUtils(PircBotX aBot) {
        if (aBot == null || bot != null) {
            throw new IllegalActionException("ATTEMPT MADE TO PERFORM ILLEGAL ACTION");
        }
        bot = aBot;
    }

    public static String toString(String[] args) {
        String result = "";
        for (String part : args) {
            result += part + " ";
        }
        return result.trim();
    }

    public static String[] toArgs(String line) {
        return line.split(" ");
    }

    public static String resolve(String html) throws MalformedURLException, IOException, URISyntaxException {
        String url = new URL(html).toURI().toString();
        URL path = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) path.openConnection();
        connection.getInputStream();
        connection.disconnect();
        html = connection.getURL().toString();
        return html;
    }

    public static String handleArgs(String message, Map<String, String> args) {
        String newMessage = message;
        for (String key : args.keySet()) {
            String convert = args.get(key);
            if (convert == null) {
                convert = "";
            }
            newMessage = newMessage.replace("{" + key + "}", convert);
        }
        return newMessage;
    }
}
