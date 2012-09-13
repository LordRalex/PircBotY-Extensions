package com.lordralex.miniabsol.MiniAbsol.api;

import org.pircbotx.hooks.events.MessageEvent;

public class CommandEvent extends MessageEvent {

    protected String command;
    protected String[] args;

    public CommandEvent(MessageEvent event) {
        super(event.getBot(), event.getChannel(), event.getUser(), event.getMessage());
        String[] split = message.split(" ");
        command = split[0].toLowerCase().trim();
        if (split.length > 1) {
            System.arraycopy(split, 1, args, 0, split.length);
        } else {
            args = new String[0];
        }
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }
}
