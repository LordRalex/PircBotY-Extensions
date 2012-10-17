package com.lordralex.ralexbot.api.events;

/**
 * @version 1.0
 * @author Joshua
 */
public class PrivateMessageEvent extends Event {

    final private String sender, login, hostname, message;

    public PrivateMessageEvent(final String aSender, final String aLogin, final String aHostname, final String aMessage) {
        sender = aSender;
        login = aLogin;
        hostname = aHostname;
        message = aMessage;
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
