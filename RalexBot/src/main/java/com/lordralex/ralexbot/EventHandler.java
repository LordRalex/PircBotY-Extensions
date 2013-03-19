/*
 * Copyright (C) 2013 Lord_Ralex
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lordralex.ralexbot;

import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.State;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.pircbotx.hooks.ListenerAdapter;

public final class EventHandler extends ListenerAdapter {

    private final List<Listener> listeners = new ArrayList<>();
    private final ConcurrentLinkedQueue<Event> queue = new ConcurrentLinkedQueue<>();
    private final EventRunner runner;
    private static final List<String> commandChars = new ArrayList<>();

    static {
        commandChars.clear();
        commandChars.add("**");
        commandChars.add("*");
    }

    public EventHandler() {
        super();
        runner = new EventRunner();
        runner.setName("Event_Runner_Thread");
    }

    public void load() {
        File extensionFolder = new File("extensions");
        File temp = new File("tempDir");
        if (temp != null && temp.listFiles() != null) {
            for (File file : temp.listFiles()) {
                if (file != null) {
                    file.delete();
                }
            }
        }
        temp.delete();
        extensionFolder.mkdirs();
        listeners.clear();
        URL[] urls = new URL[0];
        try {
            urls = new URL[]{
                extensionFolder.toURI().toURL(),
                temp.toURI().toURL()
            };
        } catch (MalformedURLException ex) {
            RalexBot.getLogger().log(Level.SEVERE, null, ex);
        }
        ClassLoader cl = new URLClassLoader(urls);
        for (File file : extensionFolder.listFiles()) {
            if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                String className = file.getName();
                loadClass(className, cl);
            } else if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file);
                    Enumeration entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();
                        if (entry.getName().contains("/")) {
                            (new File(temp, entry.getName().split("/")[0])).mkdir();
                        }
                        if (entry.isDirectory()) {
                            new File(temp, entry.getName()).mkdirs();
                            continue;
                        }
                        copyInputStream(zipFile.getInputStream(entry),
                                new BufferedOutputStream(new FileOutputStream(temp + File.separator + entry.getName())));
                    }

                } catch (IOException ex) {
                    RalexBot.getLogger().log(Level.SEVERE, "An error occured", ex);
                } finally {
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException ex) {
                            RalexBot.getLogger().log(Level.SEVERE, "An error occured", ex);
                        }
                    }
                }
            }
        }
        if (!(temp == null || temp.listFiles() == null)) {
            for (File file : temp.listFiles()) {
                if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                    String className = file.getName();
                    loadClass(className, cl);
                }
            }
        }
    }

    private void loadClass(String className, ClassLoader cl) {
        try {
            className = className.replace("tempDir" + File.separator, "").replace("extension" + File.separator, "").replace(".class", "");
            Class cls = cl.loadClass(className);
            Object obj = cls.newInstance();
            if (obj instanceof Listener) {
                Listener list = (Listener) obj;
                list.setup();
                RalexBot.getLogger().info("  Added: " + list.getClass().getName());
                list.declareValues(list.getClass());
                listeners.add(list);
            }
        } catch (Throwable ex) {
            RalexBot.getLogger().log(Level.SEVERE, "Could not add " + className, ex);
        }
    }

    private void copyInputStream(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException ex) {
            RalexBot.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void startQueue() {
        if (!runner.isAlive()) {
            runner.start();
        }
    }

    @Override
    public void onMessage(org.pircbotx.hooks.events.MessageEvent event) {
        Event nextEvt;
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
        Event nextEvt;
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
        Event nextEvt;
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
        JoinEvent nextEvt = new JoinEvent(event);
        fireEvent(nextEvt);
    }

    @Override
    public void onNickChange(org.pircbotx.hooks.events.NickChangeEvent event) throws Exception {
        NickChangeEvent nextEvt = new NickChangeEvent(event);
        fireEvent(nextEvt);
    }

    @Override
    public void onQuit(org.pircbotx.hooks.events.QuitEvent event) throws Exception {
        QuitEvent nextEvt = new QuitEvent(event);
        fireEvent(nextEvt);
    }

    @Override
    public void onPart(org.pircbotx.hooks.events.PartEvent event) throws Exception {
        PartEvent nextEvt = new PartEvent(event);
        fireEvent(nextEvt);
    }

    @Override
    public void onAction(org.pircbotx.hooks.events.ActionEvent event) throws Exception {
        ActionEvent nextEvt = new ActionEvent(event);
        fireEvent(nextEvt);
    }

    private boolean isCommand(String message) {
        for (String code : commandChars) {
            if (message.startsWith(code)) {
                return true;
            }
        }
        return false;
    }

    public void fireEvent(final Event event) {
        queue.add(event);
        runner.ping();
    }

    public void stopRunner() {
        synchronized (runner) {
            runner.interrupt();
        }
    }

    public static List<String> getCommandPrefixes() {
        List<String> clone = new ArrayList<>();
        clone.addAll(commandChars);
        return clone;
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

                                            List<String> aliases = Arrays.asList(listener.getAliases());
                                            String cmd = ((CommandEvent) next).getCommand().toLowerCase();
                                            if (listener.getAliases().length == 0
                                                    || aliases.contains(cmd)) {
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
                                    RalexBot.getLogger().log(Level.SEVERE, "Unhandled exception on event execution", e);
                                }
                            }
                        }
                    }
                }
            }
            RalexBot.getLogger().info("Ending event listener");
        }

        public void ping() {
            if (this.getState() == State.WAITING) {
                try {
                    synchronized (this) {
                        this.notify();
                    }
                } catch (IllegalMonitorStateException e) {
                    RalexBot.getLogger().log(Level.SEVERE, e.toString(), e);
                }
            }
        }
    }
}
