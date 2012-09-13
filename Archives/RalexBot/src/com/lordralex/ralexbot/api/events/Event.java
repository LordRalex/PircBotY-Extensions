package com.lordralex.ralexbot.api.events;

/**
 * @version 1.0
 * @author Joshua
 */
public abstract class Event {

    protected boolean cancelled = false;

    public final boolean isCancelled() {
        return cancelled;
    }

    public final void setCancelled(boolean newState) {
        cancelled = newState;
    }
}
