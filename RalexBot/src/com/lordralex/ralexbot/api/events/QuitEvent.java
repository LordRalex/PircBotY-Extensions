package com.lordralex.ralexbot.api.events;

/**
 * @version 1.0
 * @author Joshua
 */
public class QuitEvent extends Event {

    protected String sender, login, hostname, reason;

    public QuitEvent(final String aSender, final String aLogin, final String aHostname, final String aReason) {
        sender = aSender;
        login = aLogin;
        hostname = aHostname;
        reason = aReason;
    }

    public String getQuitter() {
        return sender;
    }

    public String getLogin() {
        return login;
    }

    public String getHostname() {
        return hostname;
    }

    public String getReason() {
        return reason;
    }
}
