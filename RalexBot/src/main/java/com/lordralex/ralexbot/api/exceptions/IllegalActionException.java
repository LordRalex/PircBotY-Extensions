/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
