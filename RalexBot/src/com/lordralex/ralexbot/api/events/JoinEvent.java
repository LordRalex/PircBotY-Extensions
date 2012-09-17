package com.lordralex.ralexbot.api.events;

public class JoinEvent extends Event {

    private final String channel;
    private final String sender;
    private final String hostname;

    public JoinEvent(org.pircbotx.hooks.events.JoinEvent event) {
        channel = event.getChannel().getName();
        sender = event.getUser().getNick();
        hostname = event.getUser().getHostmask();
    }

    public String getSender() {
        return sender;
    }

    public String getChannel() {
        return channel;
    }

    public String getHostname() {
        return hostname;
    }
}
