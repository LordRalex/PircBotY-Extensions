package com.lordralex.ralexbot.api.events;

public final class CommandEvent extends Event {

    private final String command, sender, channel, hostName;
    private final String[] args;

    public CommandEvent(org.pircbotx.hooks.events.MessageEvent event) {
        String[] temp = event.getMessage().split(" ");
        command = temp[0].substring(1).toLowerCase();
        sender = event.getUser().getNick();
        channel = event.getChannel().getName();
        hostName = event.getUser().getHostmask();
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public CommandEvent(org.pircbotx.hooks.events.PrivateMessageEvent event) {
        String[] temp = event.getMessage().split(" ");
        command = temp[0].substring(1).toLowerCase();
        sender = event.getUser().getNick();
        channel = null;
        hostName = event.getUser().getHostmask();
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public CommandEvent(org.pircbotx.hooks.events.NoticeEvent event) {
        String[] temp = event.getMessage().split(" ");
        command = temp[0].substring(1).toLowerCase();
        sender = event.getUser().getNick();
        channel = null;
        hostName = event.getUser().getHostmask();
        args = new String[temp.length - 1];
        if (temp.length >= 2) {
            System.arraycopy(temp, 1, args, 0, args.length);
        }
    }

    public String getCommand() {
        return command;
    }

    public String getSender() {
        return sender;
    }

    public String getChannel() {
        return channel;
    }

    public String[] getArgs() {
        return args;
    }

    public String getHost() {
        return hostName;
    }
}
