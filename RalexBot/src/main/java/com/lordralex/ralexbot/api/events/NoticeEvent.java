package com.lordralex.ralexbot.api.events;

public class NoticeEvent extends Event {

    private final String sender, hostname, message;

    public NoticeEvent(org.pircbotx.hooks.events.NoticeEvent event) {
        sender = event.getUser().getNick();
        hostname = event.getUser().getHostmask();
        message = event.getMessage();
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getHostname() {
        return hostname;
    }
}
