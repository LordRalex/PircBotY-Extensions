package com.lordralex.ralexbot.api.events;

import com.lordralex.ralexbot.api.users.User;

public class QuitEvent extends Event {

    private final User sender;

    public QuitEvent(org.pircbotx.hooks.events.QuitEvent event) {
        sender = User.getUser(event.getUser().getNick());
    }

    public User getSender() {
        return sender;
    }

    public String getHostname() {
        return sender.getIP();
    }
}
