/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.api.sender;

/**
 *
 * @author Joshua
 */
public interface Sender {

    public abstract void sendMessage(String message);

    public abstract void sendNotice(String message);

    public abstract void sendMessage(String[] messages);

    public abstract void sendNotice(String[] messages);
}