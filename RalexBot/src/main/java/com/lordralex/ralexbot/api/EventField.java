package com.lordralex.ralexbot.api;

import com.lordralex.ralexbot.api.events.*;

public enum EventField {

    Message(MessageEvent.class),
    Command(CommandEvent.class),
    Join(JoinEvent.class),
    NickChange(NickChangeEvent.class),
    Notice(NoticeEvent.class),
    Part(PartEvent.class),
    PrivateMessage(PrivateMessageEvent.class),
    Quit(QuitEvent.class),
    Action(ActionEvent.class);
    
    private Class eventClass;

    private EventField(Class cl) {
        eventClass = cl;
    }

    private boolean isIt(Class test) {
        if (eventClass.getName().equalsIgnoreCase(test.getName())) {
            return true;
        }
        return false;
    }

    public static EventField getEvent(Event event) {
        for (EventField evt : EventField.values()) {
            if (evt.isIt(event.getClass())) {
                return evt;
            }
        }
        return null;
    }
}
