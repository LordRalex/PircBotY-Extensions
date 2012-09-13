package com.lordralex.ralexbot.api.events;

/**
 * @version 1.0
 * @author Joshua
 */
public enum EventType {

    Join(JoinEvent.class),
    Message(MessageEvent.class),
    NickChange(NickChangeEvent.class),
    Part(PartEvent.class),
    Quit(QuitEvent.class),
    PrivateMessage(PrivateMessageEvent.class),
    Command(CommandEvent.class),
    None(Event.class);
    private final Class cl;

    private EventType(Class cl) {
        this.cl = cl;
    }

    public static EventType getEvent(Event event) {
        for (EventType type : getValues()) {
            if (type.cl.isInstance(event)) {
                return type;
            }
        }
        return null;
    }

    public static EventType[] getValues() {
        return new EventType[]{
                    Join,
                    Message,
                    NickChange,
                    Part,
                    Quit,
                    PrivateMessage,
                    Command
                };
    }
}
