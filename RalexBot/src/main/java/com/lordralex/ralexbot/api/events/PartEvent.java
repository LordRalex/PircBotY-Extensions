package com.lordralex.ralexbot.api.events;

import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.users.User;

public class PartEvent extends Event {

    private final User sender;
    private final Channel channel;

    public PartEvent(org.pircbotx.hooks.events.PartEvent event) {
        sender = User.getUser(event.getUser().getNick());
        channel = Channel.getChannel(event.getChannel().getName());
    }

    public Channel getChannel() {
        return channel;
    }

    public User getSender() {
        return sender;
    }

    public String getHostname() {
        return sender.getIP();
    }
}
