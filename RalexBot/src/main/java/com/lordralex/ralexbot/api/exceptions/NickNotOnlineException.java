package com.lordralex.ralexbot.api.exceptions;

import org.pircbotx.exception.IrcException;

/**
 *
 * @author Joshua
 */
public class NickNotOnlineException extends IrcException {

    public NickNotOnlineException(String message) {
        super(message);
    }

    public NickNotOnlineException() {
        this("User is not online");
    }
}
