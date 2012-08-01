package com.lordralex.ralexbot.api;

import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.RalexBotMain;
import com.lordralex.ralexbot.api.events.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.jibble.pircbot.User;

/**
 * @version 1.0
 * @author Joshua
 */
public abstract class Listener {

    public Map<EventType, Priority> priorities = new HashMap<>();

    public void runEvent(Event event) {
        if (event instanceof MessageEvent) {
            onMessage((MessageEvent) event);
        } else if (event instanceof JoinEvent) {
            onJoin((JoinEvent) event);
        } else if (event instanceof PrivateMessageEvent) {
            onPrivateMessage((PrivateMessageEvent) event);
        } else if (event instanceof PartEvent) {
            onPart((PartEvent) event);
        } else if (event instanceof QuitEvent) {
            onQuit((QuitEvent) event);
        } else if (event instanceof NickChangeEvent) {
            onNickChange((NickChangeEvent) event);
        } else if (event instanceof CommandEvent) {
            CommandEvent cmdEvent = (CommandEvent) event;
            if (getAliases() == null || getAliases().length == 0) {
                onCommand(cmdEvent);
            } else {
                for (String alias : getAliases()) {
                    if (alias.equalsIgnoreCase(cmdEvent.getCommand())) {
                        onCommand(cmdEvent);
                        return;
                    }
                }
            }
        }
    }

    public abstract void declarePriorities();

    /**
     * This is fired whenever a message is sent to a channel by anyone. This
     * does nothing, but can be overridden if additional uses are needed when a
     * message is sent. This will fire after the {@link CommandManager} has
     * already passed the command on to a {@link CommandExecutor} and so will
     * occur after a command, if one was found.
     *
     * @param channel The channel the message is from.
     * @param sender The nickname that sent the message.
     * @param login The login of the person who sent the message.
     * @param hostname The host name of the person who sent the message
     * @param message The entire message that was sent by the sender.
     */
    public void onMessage(MessageEvent event) {
    }

    /**
     * This is fired when anyone joins a channel the bot is in. This does
     * nothing, but can be overridden with each {@link CommandExecutor} that is
     * defined in the {@link RalexBot} where each will be ran sequentially.
     *
     * @param channel The channel the message is from.
     * @param sender The nickname that sent the message.
     * @param login The login of the person who sent the message.
     * @param hostname The host name of the person who sent the message
     */
    public void onJoin(JoinEvent event) {
    }

    /**
     * This is fired when anyone joins a channel the bot is in. This does
     * nothing, but can be overridden with each {@link CommandExecutor} that is
     * defined in the {@link RalexBot} where each will be ran sequentially.
     *
     * @param sender The nickname that sent the message.
     * @param login The login of the person who sent the message.
     * @param hostname The host name of the person who sent the message
     * @param message The entire message that was sent by the sender.
     */
    public void onPrivateMessage(PrivateMessageEvent event) {
    }

    /**
     * This is fired when anyone parts a channel the bot is in. This does
     * nothing, but can be overridden with each {@link CommandExecutor} that is
     * defined in the {@link RalexBot} where each will be ran sequentially.
     *
     * @param channel The channel the message is from.
     * @param sender The nickname that sent the message.
     * @param login The login of the person who sent the message.
     * @param hostname The host name of the person who sent the message
     */
    public void onPart(PartEvent event) {
    }

    /**
     * This is fired when anyone quits the network the bot is in. This does
     * nothing, but can be overridden with each {@link CommandExecutor} that is
     * defined in the {@link RalexBot} where each will be ran sequentially.
     *
     * @param sourceNick The nick the person who quit was using
     * @param sourceLogin The login of the person who quit
     * @param sourceHostname The hostname of the person who quit
     * @param reason The reason when quitting
     */
    public void onQuit(QuitEvent event) {
    }

    /**
     *
     * @param oldNick The old nick of the user
     * @param login The login name of the user
     * @param hostname The hostname of the user
     * @param newNick The new nick of the user
     */
    public void onNickChange(NickChangeEvent event) {
    }

    /**
     * The command system. This is fired from the main driver and runs when the
     * core has found a command. This only will though fire the onCommand()
     * method which the core determined is the most appropriate one to use
     *
     * @param command The name of the command that is used.
     * @param sender The name of the user of the command.
     * @param channel The channel the command was used in.
     * @param args The arguments that was sent with the command
     */
    public void onCommand(CommandEvent event) {
    }

    /**
     * Returns the aliases the command can be triggered by. Multiple aliases can
     * be specified and each will trigger the command assuming they are not
     * overridden by another {@link CommandExecutor}
     *
     * @return
     */
    public String[] getAliases() {
        return new String[0];
    }

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

    public String parse(String html) throws MalformedURLException, IOException, URISyntaxException {
        String url = html.replace(" ", "%20");
        URL path = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) path.openConnection();
        connection.getInputStream();
        html = connection.getURL().toString();
        return html;
    }
}
