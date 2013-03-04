package com.lordralex.ralexbot.api;

import com.lordralex.ralexbot.api.exceptions.IllegalActionException;
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
