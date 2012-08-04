package com.lordralex.ralexbot.api.apihandlers;

import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.Event;
import com.lordralex.ralexbot.api.events.EventType;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.0
 * @author Joshua
 */
public class EventManager {

    List<Listener> listeners = new ArrayList<>();

    public EventManager() {
        listeners = new ArrayList<>();
    }

    public void loadExecutors() throws MalformedURLException {
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
                        list.declarePriorities();
                        listeners.add(list);
                        System.out.println("  Added: " + list.getClass().getName());
                    }
                } catch (Exception ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void runEvent(Event eventToRun) {
        EventType eventType = EventType.getEvent(eventToRun);
        for (Priority prio : Priority.getValues()) {
            for (Listener listener : listeners) {
                if (listener.priorities.get(eventType) == prio) {
                    listener.runEvent(eventToRun);
                }
            }
        }
    }
}
