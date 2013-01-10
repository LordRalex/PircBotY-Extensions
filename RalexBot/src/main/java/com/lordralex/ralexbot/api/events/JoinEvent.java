package com.lordralex.ralexbot.api.events;

import com.lordralex.ralexbot.api.users.User;

public class JoinEvent extends Event {

    private final String channel;
    private final User sender;

    public JoinEvent(org.pircbotx.hooks.events.JoinEvent event) {
        channel = event.getChannel().getName();
        sender = User.getUser(event.getUser().getNick());
    }

    public User getSender() {
        return sender;
    }

    public String getChannel() {
        return channel;
    }

    public String getHostname() {
        return sender.getIP();
    }
}
