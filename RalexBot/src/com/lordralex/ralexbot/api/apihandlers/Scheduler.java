package com.lordralex.ralexbot.api.apihandlers;

import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.Event;
import com.lordralex.ralexbot.api.events.EventType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Joshua
 */
public class Scheduler {

    List<Listener> listeners;
    private Integer counter;
    Map<Integer, Runnable> threads;

    public Scheduler(List<Listener> list) {
        listeners = list;
        counter = 0;
        threads = new HashMap<>();
    }

    public int scheduleEvent(Event evt) {
        counter++;
        Runnable thread = new Runnable(counter, evt);
        threads.put(counter, thread);
        thread.start();
        return counter;
    }

    private void removeThread(Integer id) {
        threads.remove(id);
    }

    public boolean isRunning(Integer id) {
        return threads.containsKey(id);
    }

    private class Runnable extends Thread {

        Integer id;
        Event eventToRun;

        public Runnable(Integer val, Event event) {
            id = val;
            eventToRun = event;
        }

        @Override
        public void run() {
            EventType eventType = EventType.getEvent(eventToRun);
            for (Priority prio : Priority.getValues()) {
                for (Listener listener : listeners) {
                    if (listener.priorities.get(eventType) == prio) {
                        listener.runEvent(eventToRun);
                    }
                }
            }
            removeThread(id);
        }
    }
}