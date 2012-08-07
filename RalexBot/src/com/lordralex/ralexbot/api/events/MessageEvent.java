package com.lordralex.ralexbot.api.events;

/**
 * @version 1.0
 * @author Joshua
 */
public class MessageEvent extends Event {

    final private String channel, sender, login, hostname, message;

    public MessageEvent(final String aChannel, final String aSender, final String aLogin, final String aHostname, final String aMessage) {
        channel = aChannel;
        sender = aSender;
        login = aLogin;
        hostname = aHostname;
        message = aMessage;
    }

    public String getChannel() {
        return channel;
    }

    public String getSender() {
        return sender;
    }

    public String getLogin() {
        return login;
    }

    public String getHostname() {
        return hostname;
    }

    public String getMessage() {
        return message;
    }
}
