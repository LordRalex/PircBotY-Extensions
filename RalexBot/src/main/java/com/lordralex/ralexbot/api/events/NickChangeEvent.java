package com.lordralex.ralexbot.api.events;

import com.lordralex.ralexbot.api.users.User;

public class NickChangeEvent extends Event {

    private final String oldNick, newNick, hostname;
    private final User sender;

    public NickChangeEvent(org.pircbotx.hooks.events.NickChangeEvent event) {
        oldNick = event.getOldNick();
        newNick = event.getNewNick();
        hostname = event.getUser().getHostmask();
        sender = User.getUser(newNick);
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

    public User getUser() {
        return sender;
    }
}
