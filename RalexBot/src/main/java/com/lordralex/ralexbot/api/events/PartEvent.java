package com.lordralex.ralexbot.api.events;

public class PartEvent extends Event {

    private final String sender, channel, hostname;

    public PartEvent(org.pircbotx.hooks.events.PartEvent event) {
        sender = event.getUser().getNick();
        hostname = event.getUser().getHostmask();
        channel = event.getChannel().getName();
    }

    public String getChannel() {
        return channel;
    }

    public String getSender() {
        return sender;
    }

    public String getHostname() {
        return hostname;
    }
}
