package com.lordralex.ralexbot.api.events;

/**
 * @version 1.0
 * @author Joshua
 */
public class CommandEvent extends Event {

    private String command, sender, channel;
    private String[] args;

    public CommandEvent(String aCommand, String aSender, String aChannel, String[] lines) {
        command = aCommand;
        sender = aSender;
        channel = aChannel;
        args = lines;
    }

    public String[] getArgs() {
        return args;
    }

    public String getChannel() {
        return channel;
    }

    public String getSender() {
        return sender;
    }

    public String getCommand() {
        return command;
    }
}
