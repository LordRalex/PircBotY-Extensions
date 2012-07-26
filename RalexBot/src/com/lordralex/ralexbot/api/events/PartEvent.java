package com.lordralex.ralexbot.api.events;

/**
 * @version 1.0
 * @author Joshua
 */
public class PartEvent extends Event {

    protected String channel;
    protected String sender;
    protected String login;
    protected String hostname;

    public PartEvent(final String aChannel, final String aSender, final String aLogin, final String aHostname) {
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
