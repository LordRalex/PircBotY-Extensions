package com.lordralex.ralexbot.api;

import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.RalexBotMain;
import org.jibble.pircbot.User;

/**
 * @version 1.0
 * @author Joshua
 */
public class Utils {

    /**
     * Checks to see if the specified user is the bot's master. This is
     * hard-coded in, where null is the console.
     *
     * @param user The user to test.
     * @return True if user is the master, otherwise false
     */
    public boolean isMaster(String user) {
        if (user == null || user.equalsIgnoreCase("lord_ralex") || user.equalsIgnoreCase("console")) {
            return true;
        }
        return false;
    }

    /**
     * This will replace certain placeholders in a string. <p> This only will
     * change {User}, {Channel}, and {#}.
     *
     * @param sender The person which replaces {User}
     * @param channel The channel to replace {Channel} with
     * @param message The message to edit
     * @param args The arguments that are also passed
     * @return The new string with placeholders filled with provided information
     */
    public String replacePlaceHolders(String sender, String channel, String message, String[] args) {
        if (sender == null) {
            sender = "console";
        }
        message = message.replace("{User}", sender);
        message = message.replace("{Channel}", channel);
        try {
            for (int i = 0; i < args.length; i++) {
                message = message.replace("{" + i + "}", args[i]);
            }
        } catch (IndexOutOfBoundsException e) {
        }
        return message;
    }

    /**
     * This looks to see if a nick is op in a specific channel the bot is in.
     *
     * @param name The nick of the person to test.
     * @param channel The channel to see if they are op in
     * @return True if the nick is op in the channel, false otherwise
     */
    public boolean isOP(String name, String channel) {
        if (name == null || channel == null) {
            return false;
        }
        User[] users = RalexBotMain.getBot().getUsers(channel);
        for (User user : users) {
            if (user.getNick().equalsIgnoreCase(name)) {
                return user.isOp();
            }
        }
        return false;
    }

    /**
     * This looks to see if a nick is voiced in a specific channel the bot is
     * in.
     *
     * @param name The nick of the person to test.
     * @param channel The channel to see if they are voiced in
     * @return True if the nick is voiced in the channel, false otherwise
     */
    public boolean isVoice(String name, String channel) {
        if (name == null || channel == null) {
            return false;
        }
        User[] users = RalexBotMain.getBot().getUsers(channel);
        for (User user : users) {
            if (user.getNick().equalsIgnoreCase(name)) {
                return user.hasVoice();
            }
        }
        return false;
    }

    /**
     * Sends a single message to a person or channel.
     *
     * @param target The nick or channel to send the message to.
     * @param message The message to send to the user/channel.
     */
    public void sendMessage(String target, String message) {
        if (target == null) {
            return;
        }
        RalexBotMain.getBot().sendMessage(target, message.trim());
    }

    /**
     * Sends a collections of messages to a person or channel. Each line in the
     * array will be sent to the target assuming the target is not null.
     *
     * @param target The channel or person to send the messages to.
     * @param message The collection of messages to send.
     */
    public void sendMessage(String target, String[] message) {
        if (target == null) {
            return;
        }
        for (String line : message) {
            sendMessage(target, line);
        }
    }

    public void sendNotice(String target, String message) {
        if (target == null) {
            return;
        }
        getBot().sendNotice(target, message);
    }

    /**
     * Return the static bot from the driver
     *
     * @return The bot from {@link RalexBotMain}
     */
    public RalexBot getBot() {
        return RalexBotMain.getBot();
    }

    public String buildArgs(String[] args) {
        String total = "";
        for (String string : args) {
            total += string + " ";
        }
        return total.trim();
    }
}
