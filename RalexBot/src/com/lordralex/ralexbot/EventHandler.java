package com.lordralex.ralexbot;

import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.events.Event;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
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
                            //list.setup();
                            //list.declarePriorities();
                            listeners.add(list);
                            System.out.println("  Added: " + list.getClass().getName());
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        } catch (MalformedURLException ex) {
        }
        runner = new EventRunner();
    }

    private void fireEvent(final Event event) {
        queue.add(event);
    }

    private class EventRunner extends Thread {

        public EventRunner() {
        }

        @Override
        public void run() {
            while(driver.isConnected())
            {
                Event next = queue.poll();
                if(next != null)
                {
                    
                }
            }
        }
    }
}
