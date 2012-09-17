package com.lordralex.ralexbot.api.events;

public class QuitEvent extends Event {

    private final String sender, hostname;

    public QuitEvent(org.pircbotx.hooks.events.QuitEvent event) {
        sender = event.getUser().getNick();
        hostname = event.getUser().getHostmask();
    }

    public String getSender() {
        return sender;
    }

    public String getHostname() {
        return hostname;
    }
}
