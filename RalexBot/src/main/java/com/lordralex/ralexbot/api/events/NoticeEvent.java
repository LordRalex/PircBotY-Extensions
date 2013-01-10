package com.lordralex.ralexbot.api.events;

import com.lordralex.ralexbot.api.users.User;

public class NoticeEvent extends Event {

    private final String message;
    private final User sender;

    public NoticeEvent(org.pircbotx.hooks.events.NoticeEvent event) {
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
