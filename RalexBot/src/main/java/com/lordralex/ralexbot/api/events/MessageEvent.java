package com.lordralex.ralexbot.api.events;

public final class MessageEvent extends Event {

    private final String sender, channel, message, hostName;

    public MessageEvent(org.pircbotx.hooks.events.MessageEvent event) {
        sender = event.getUser().getNick();
        channel = event.getChannel().getName();
        message = event.getMessage();
        hostName = event.getUser().getHostmask();
    }

    public String getSender() {
        return sender;
    }

    public String getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    public String getHostname() {
        return hostName;
    }
}
