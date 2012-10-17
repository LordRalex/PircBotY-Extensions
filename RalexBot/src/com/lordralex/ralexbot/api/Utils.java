/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

/**
 *
 * @author Joshua
 */
public final class Utils {

    private static PircBotX bot;

    public static void setUtils(PircBotX aBot) {
        if (aBot == null || bot != null) {
            throw new RuntimeException("ATTEMPT MADE TO PERFORM ILLEGAL ACTION");
        }
        bot = aBot;
    }

    public static void setNick(String newNick) {
        bot.changeNick(newNick);
    }

    public static String getNick() {
        return bot.getNick();
    }

    public static void kick(String nick, String channel) {
        if (bot.getUserBot().isIrcop()) {
            bot.kick(bot.getChannel(channel), bot.getUser(nick));
        } else {
            bot.sendMessage("chanserv", "kick " + channel + " " + nick);
        }
    }

    public static void kick(String nick, String channel, String reason) {
        if (reason == null || reason.isEmpty()) {
            kick(nick, channel);
            return;
        }
        if (bot.getChannel(channel).isOp(bot.getUserBot())) {
            bot.kick(bot.getChannel(channel), bot.getUser(nick), reason);
        } else {
            bot.sendMessage("chanserv", "kick " + channel + " " + nick + " " + reason);
        }
    }

    public static void joinChannel(String channel) {
        bot.joinChannel(channel);
    }

    public static void leaveChannel(String channel) {
        bot.partChannel(bot.getChannel(channel));
    }

    public static void sendMessage(String target, String message) {
        if (target == null || message == null || message.trim().isEmpty()) {
            throw new NullPointerException("Target and message must exist");
        }
        bot.sendMessage(target, message);
    }

    public static void sendNotice(String target, String notice) {
        if (target == null || notice == null || notice.trim().isEmpty()) {
            throw new NullPointerException("Target and notice must exist");
        }
        bot.sendNotice(target, notice);
    }

    public static void sendMessage(String target, String[] messages) {
        if (target == null || messages == null || messages.length == 0) {
            throw new NullPointerException("Target and messages must exist");
        }
        for (String message : messages) {
            bot.sendMessage(target, message);
        }
    }

    public static void sendNotice(String target, String[] notices) {
        if (target == null || notices == null || notices.length == 0) {
            throw new NullPointerException("Target and notices must exist");
        }
        for (String notice : notices) {
            bot.sendNotice(target, notice);
        }
    }

    public static boolean hasOP(String user, String channel) {
        return bot.getChannel(channel).isOp(bot.getUser(user));
    }

    public static boolean hasVoice(String user, String channel) {
        boolean is = bot.getChannel(channel).hasVoice(bot.getUser(user));
        if (!is) {
            is = hasOP(user, channel);
        }
        return is;
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

    public static String parse(String html) throws MalformedURLException, IOException, URISyntaxException {
        String url = html.replace(" ", "%20");
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

    public static String[] getUsers(String channel) {
        if (channel == null) {
            return new String[0];
        }
        Set<User> users = bot.getUsers(bot.getChannel(channel));
        List<String> names = new ArrayList<String>();
        for (User user : users) {
            names.add(user.getNick());
        }
        return names.toArray(new String[names.size()]);
    }

    private Utils() {
    }
}
