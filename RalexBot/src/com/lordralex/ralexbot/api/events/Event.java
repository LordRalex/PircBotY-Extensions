package com.lordralex.ralexbot.api.events;

/**
 * @version 1.0
 * @author Joshua
 */
public class Event {

    protected boolean cancelled = false;

    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled(boolean newState)
    {
        cancelled = newState;
    }
}
