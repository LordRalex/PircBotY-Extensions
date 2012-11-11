package com.lordralex.ralexbot.api.events;

public abstract class Event {

    private boolean cancelled = false;

    public final void setCancelled(boolean state) {
        cancelled = state;
    }

    public final boolean isCancelled()
    {
        return cancelled;
    }
}
