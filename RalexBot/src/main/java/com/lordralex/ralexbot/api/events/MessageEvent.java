package com.lordralex.ralexbot.api.events;

import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.users.User;

public final class MessageEvent extends Event {

    private final String message;
    private final User sender;
    private final Channel channel;

    public MessageEvent(org.pircbotx.hooks.events.MessageEvent event) {
        sender = User.getUser(event.getUser().getNick());
        channel = Channel.getChannel(event.getChannel().getName());
        message = event.getMessage();
    }

    public User getSender() {
        return sender;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    public String getHostname() {
        return sender.getIP();
    }
}
