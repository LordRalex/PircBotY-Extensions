package com.lordralex.ralexbot.api.events;

/**
 * @version 1.0
 * @author Joshua
 */
public class NickChangeEvent extends Event {

    final private String oldNick, newNick, login, hostname;

    public NickChangeEvent(final String aOldNick, final String aLogin, final String aHostname, final String aNewNick) {
        oldNick = aOldNick;
        newNick = aNewNick;
        login = aLogin;
        hostname = aHostname;
    }

    public String getOldNick() {
        return oldNick;
    }

    public String getNewNick() {
        return newNick;
    }

    public String getLogin() {
        return login;
    }

    public String getHostname() {
        return hostname;
    }
}
