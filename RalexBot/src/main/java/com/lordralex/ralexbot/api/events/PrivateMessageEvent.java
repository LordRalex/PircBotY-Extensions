package com.lordralex.ralexbot.api.events;

import com.lordralex.ralexbot.api.users.User;

public class PrivateMessageEvent extends Event {

    private final String message;
    private final User sender;

    public PrivateMessageEvent(org.pircbotx.hooks.events.PrivateMessageEvent event) {
        sender = User.getUser(event.getUser().getNick());
        message = event.getMessage();
    }

    public User getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getHostname() {
        return sender.getIP();
    }
}
