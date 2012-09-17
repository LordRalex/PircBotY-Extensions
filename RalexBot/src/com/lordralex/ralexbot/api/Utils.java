/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.api;

import org.pircbotx.PircBotX;

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
        if (bot.getUserBot().isIrcop()) {
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
}
