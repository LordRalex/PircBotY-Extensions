package com.lordralex.ralexbot.api.events;

public class PrivateMessageEvent extends Event{

    private final String sender, hostname, message;

    public PrivateMessageEvent(org.pircbotx.hooks.events.PrivateMessageEvent event) {
        sender = event.getUser().getNick();
        hostname = event.getUser().getHostmask();
        message = event.getMessage();
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getHostname() {
        return hostname;
    }

}
