package com.lordralex.miniabsol.MiniAbsol.managers;

import com.lordralex.miniabsol.MiniAbsol.MiniAbsol;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.ListenerAdapter;

public class EventManager extends ListenerAdapter{

    MiniAbsol bot;

    public EventManager(MiniAbsol instance) {
        bot = instance;
    }

    public synchronized void runEvent(Event event) {
    }
}
