package com.lordralex.ralexbot;

import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.*;
import java.io.File;
import java.lang.Thread.State;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.hooks.ListenerAdapter;

public final class EventHandler extends ListenerAdapter {

    private List<Listener> listeners = new ArrayList<Listener>();
    private ConcurrentLinkedQueue<Event> queue = new ConcurrentLinkedQueue<Event>();
    EventRunner runner;
    private static final List<Character> commandChars = new ArrayList<Character>();

    static {
        commandChars.clear();
        commandChars.add('*');
    }

    public EventHandler() {
        super();
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
                            list.setup();
                            list.declareValues(list.getClass());
                            listeners.add(list);
                            System.out.println("  Added: " + list.getClass().getName());
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(EventHandler.class.getName(), null).log(Level.SEVERE, "", ex);
                    }
                }
            }
        } catch (MalformedURLException ex) {
        }
        runner = new EventRunner();
        runner.setName("Event_Handler_Thread");
    }

    public void startQueue() {
        if (!runner.isAlive()) {
            runner.start();
        }
    }

    @Override
    public void onMessage(org.pircbotx.hooks.events.MessageEvent event) {
        Event nextEvt = null;
        if (isCommand(event.getMessage())) {
            nextEvt = new CommandEvent(event);
        } else {
            nextEvt = new MessageEvent(event);
        }

        if (nextEvt != null) {
            fireEvent(nextEvt);
        }
    }

    @Override
    public void onPrivateMessage(org.pircbotx.hooks.events.PrivateMessageEvent event) throws Exception {
        Event nextEvt = null;
        if (isCommand(event.getMessage())) {
            nextEvt = new CommandEvent(event);
        } else {
            nextEvt = new PrivateMessageEvent(event);
        }

        if (nextEvt != null) {
            fireEvent(nextEvt);
        }
    }

    @Override
    public void onNotice(org.pircbotx.hooks.events.NoticeEvent event) throws Exception {
        Event nextEvt = null;
        if (isCommand(event.getMessage())) {
            nextEvt = new CommandEvent(event);
        } else {
            nextEvt = new NoticeEvent(event);
        }

        if (nextEvt != null) {
            fireEvent(nextEvt);
        }
    }

    @Override
    public void onJoin(org.pircbotx.hooks.events.JoinEvent event) throws Exception {
        Event nextEvt = new JoinEvent(event);
        fireEvent(nextEvt);
    }

    @Override
    public void onNickChange(org.pircbotx.hooks.events.NickChangeEvent event) throws Exception {
        super.onNickChange(event);
    }

    @Override
    public void onQuit(org.pircbotx.hooks.events.QuitEvent event) throws Exception {
        Event nextEvt = new QuitEvent(event);
        fireEvent(nextEvt);
    }

    @Override
    public void onPart(org.pircbotx.hooks.events.PartEvent event) throws Exception {
        Event nextEvt = new PartEvent(event);
        fireEvent(nextEvt);
    }

    private boolean isCommand(String message) {
        return commandChars.contains(message.charAt(0));
    }

    private void fireEvent(final Event event) {
        queue.add(event);
        runner.ping();
    }

    public void stopRunner() {
        runner.interrupt();
    }

    private class EventRunner extends Thread {

        @Override
        public void run() {
            boolean run = true;
            while (run) {
                Event next = queue.poll();
                if (next == null) {
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException ex) {
                            run = false;
                        }
                    }
                } else if (next != null) {
                    EventField type = EventField.getEvent(next);
                    if (type == null) {
                        break;
                    }
                    for (Priority prio : Priority.values()) {
                        for (Listener listener : listeners) {
                            EventType info = listener.priorities.get(type);
                            if (info == null) {
                                continue;
                            }
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
                                        case Command:
                                            if (Arrays.asList(listener.getAliases()).contains(((CommandEvent) next).getCommand().toLowerCase())) {
                                                listener.runEvent((CommandEvent) next);
                                            }
                                            break;
                                        case Join:
                                            listener.runEvent((JoinEvent) next);
                                            break;
                                        case NickChange:
                                            listener.runEvent((NickChangeEvent) next);
                                            break;
                                        case Notice:
                                            listener.runEvent((NoticeEvent) next);
                                            break;
                                        case Part:
                                            listener.runEvent((PartEvent) next);
                                            break;
                                        case PrivateMessage:
                                            listener.runEvent((PrivateMessageEvent) next);
                                            break;
                                        case Quit:
                                            listener.runEvent((QuitEvent) next);
                                            break;
                                    }
                                } catch (Exception e) {
                                    Logger.getLogger(EventHandler.class.getName(), null).log(Level.SEVERE, "Unhandled exception on event execution", e);
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Ending event listener");
        }

        public void ping() {
            if (this.getState() == State.WAITING) {
                try {
                    synchronized (this) {
                        this.notify();
                    }
                } catch (IllegalMonitorStateException e) {
                    Logger.getLogger(EventHandler.class.getName(), null).log(Level.SEVERE, e.toString(), e);
                }
            }
        }
    }
}
