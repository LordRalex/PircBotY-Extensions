package com.lordralex.ralexbot;

import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.*;
import com.sun.istack.internal.logging.Logger;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;

public final class EventHandler extends ListenerAdapter {

    private List<Listener> listeners = new ArrayList<>();
    private ConcurrentLinkedQueue<Event> queue = new ConcurrentLinkedQueue<>();
    private PircBotX driver;
    EventRunner runner;

    public EventHandler(PircBotX bot) {
        super();
        driver = bot;
        try {
            File extensionFolder = new File("extensions");
            extensionFolder.mkdirs();
            listeners.clear();
            URL[] urls = new URL[]{extensionFolder.toURI().toURL()};
            ClassLoader cl = new URLClassLoader(urls);
            for (File file : extensionFolder.listFiles()) {
                if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                    try {
                        String className = file.getName();
                        className = className.replace("extension" + File.separator, "").replace(".class", "");
                        Class cls = cl.loadClass(className);
                        Object obj = cls.newInstance();
                        if (obj instanceof Listener) {
                            Listener list = (Listener) obj;
                            list.declareValues(list.getClass());
                            listeners.add(list);
                            System.out.println("  Added: " + list.getClass().getName());
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                    }
                }
            }
        } catch (MalformedURLException ex) {
        }
        runner = new EventRunner();
    }

    @Override
    public void onMessage(org.pircbotx.hooks.events.MessageEvent event) {
        Event nextEvt = null;
        if (event.getMessage().startsWith("*")) {
            nextEvt = new CommandEvent(event);
        } else {
            nextEvt = new MessageEvent(event);
        }

        if (nextEvt != null) {
            fireEvent(nextEvt);
        }
    }

    private void fireEvent(final Event event) {
        queue.add(event);
    }

    private class EventRunner extends Thread {

        public EventRunner() {
        }

        @Override
        public void run() {
            while (driver.isConnected()) {
                Event next = queue.poll();
                if (next != null) {
                    EventField type = EventField.getEvent(next);
                    if (type == null) {
                        break;
                    }
                    for (Priority prio : Priority.values()) {
                        for (Listener listener : listeners) {
                            EventType info = listener.priorities.get(type);
                            if (next.isCancelled() && !info.ignoreCancel()) {
                                continue;
                            }
                            Priority temp = info.priority();
                            if (temp != null && temp == prio) {
                                try {
                                    switch (type) {
                                        case Message:
                                            listener.runEvent((MessageEvent) next);
                                            break;
                                    }
                                } catch (Exception e) {
                                    Logger.getLogger(EventHandler.class).log(Level.SEVERE, "Unhandled exception while running event", e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
