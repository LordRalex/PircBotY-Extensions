/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.api.events;

import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.users.User;

/**
 *
 * @author Joshua
 */
public class ActionEvent extends Event {

    private final Channel channel;
    private final User sender;
    private final String action;

    public ActionEvent(org.pircbotx.hooks.events.ActionEvent event) {
        channel = Channel.getChannel(event.getChannel().getName());
        sender = User.getUser(event.getUser().getNick());
        action = event.getMessage();
    }

    public String getAction() {
        return action;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getSender() {
        return sender;
    }
}
