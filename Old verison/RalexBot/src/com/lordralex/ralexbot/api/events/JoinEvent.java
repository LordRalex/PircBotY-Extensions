package com.lordralex.ralexbot.api.events;

/**
 * @version 1.0
 * @author Joshua
 */
public class JoinEvent extends Event {

    final private String channel, sender, login, hostname;

    public JoinEvent(final String aChannel, final String aSender, final String aLogin, final String aHostname) {
        channel = aChannel;
        sender = aSender;
        login = aLogin;
        hostname = aHostname;
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
}
