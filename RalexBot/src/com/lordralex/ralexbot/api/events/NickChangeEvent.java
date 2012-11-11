package com.lordralex.ralexbot.api.events;

public class NickChangeEvent extends Event {

    private final String oldNick, newNick, hostname;

    public NickChangeEvent(org.pircbotx.hooks.events.NickChangeEvent event) {
        oldNick = event.getOldNick();
        newNick = event.getNewNick();
        hostname = event.getUser().getHostmask();
    }

    public String getOldNick() {
        return oldNick;
    }

    public String getNewNick() {
        return newNick;
    }

    public String getHostname() {
        return hostname;
    }
}
