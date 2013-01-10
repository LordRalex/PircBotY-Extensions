package com.lordralex.ralexbot.api.exceptions;

/**
 *
 * @author Joshua
 */
public class IllegalActionException extends RuntimeException {

    public IllegalActionException() {
        this("ATTEMPT MADE TO PERFORM ILLEGAL ACTION");
    }

    public IllegalActionException(String message) {
        super(message);
    }
}
