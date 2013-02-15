package com.lordralex.ralexbot.api.events;

import com.lordralex.ralexbot.EventHandler;
import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.exceptions.NickNotOnlineException;
import com.lordralex.ralexbot.api.users.User;

public final class CommandEvent extends Event {

    private final String command;
    private final User sender;
    private final Channel channel;
    private final String[] args;

    public CommandEvent(org.pircbotx.hooks.events.MessageEvent event) {
        String[] temp = event.getMessage().split(" ");
        String commandTemp = temp[0].toLowerCase();
        for (String cmd : EventHandler.getCommandPrefixes()) {
            if (commandTemp.startsWith(cmd)) {
                commandTemp = commandTemp.replaceFirst(cmd, "");
                break;
            }
        }

        command = commandTemp;
        sender = User.getUser(event.getUser().getNick());
        channel = Channel.getChannel(event.getChannel().getName());
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public CommandEvent(org.pircbotx.hooks.events.PrivateMessageEvent event) {
        String[] temp = event.getMessage().split(" ");
        command = temp[0].substring(1).toLowerCase();
        sender = User.getUser(event.getUser().getNick());
        channel = null;
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public CommandEvent(org.pircbotx.hooks.events.NoticeEvent event) throws NickNotOnlineException {
        String[] temp = event.getMessage().split(" ");
        command = temp[0].substring(1).toLowerCase();
        sender = User.getUser(event.getUser().getNick());
        channel = null;
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public String getCommand() {
        return command;
    }

    public User getSender() {
        return sender;
    }

    public Channel getChannel() {
        return channel;
    }

    public String[] getArgs() {
        return args;
    }
}
