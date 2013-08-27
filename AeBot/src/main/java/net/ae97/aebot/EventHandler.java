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
package net.ae97.aebot;

import net.ae97.aebot.api.events.JoinEvent;
import net.ae97.aebot.api.events.ActionEvent;
import net.ae97.aebot.api.events.PermissionEvent;
import net.ae97.aebot.api.events.PrivateMessageEvent;
import net.ae97.aebot.api.events.NickChangeEvent;
import net.ae97.aebot.api.events.CancellableEvent;
import net.ae97.aebot.api.events.NoticeEvent;
import net.ae97.aebot.api.events.ConnectionEvent;
import net.ae97.aebot.api.events.KickEvent;
import net.ae97.aebot.api.events.PartEvent;
import net.ae97.aebot.api.events.QuitEvent;
import net.ae97.aebot.api.events.Event;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.events.MessageEvent;
import net.ae97.aebot.api.EventField;
import static net.ae97.aebot.api.EventField.Kick;
import net.ae97.aebot.api.EventType;
import net.ae97.aebot.api.Listener;
import net.ae97.aebot.api.Priority;
import net.ae97.aebot.api.users.User;
import net.ae97.aebot.settings.Settings;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.UserSnapshot;
import org.pircbotx.hooks.ListenerAdapter;

public final class EventHandler extends ListenerAdapter {

    private final List<Listener> listeners = new ArrayList<>();
    private final ConcurrentLinkedQueue<Event> queue = new ConcurrentLinkedQueue<>();
    private final EventRunner runner;
    private static final List<CommandPrefix> commandChars = new ArrayList<>();
    private final PircBotX masterBot;
    private ClassLoader classLoader;
    private final ExecutorService execServ;
    private final Map<Listener, Map<EventField, EventType>> priorities = new ConcurrentHashMap<>();

    public EventHandler(PircBotX bot) {
        super();
        masterBot = bot;
        runner = new EventRunner();
        runner.setName("Event_Runner_Thread");
        List<String> settings = Settings.getGlobalSettings().getStringList("command-prefix");
        commandChars.clear();
        if (settings.isEmpty()) {
            settings.add("**");
        }
        for (String commandChar : settings) {
            String[] args = commandChar.split("\\|");
            String prefix = args[0];
            String owner;
            if (args.length == 1) {
                owner = null;
            } else {
                owner = args[1];
            }
            AeBot.log(Level.INFO, "Adding command prefix: " + prefix + (owner == null ? "" : " ( " + owner + ")"));
            commandChars.add(new CommandPrefix(prefix, owner));
        }
        execServ = Executors.newFixedThreadPool(5);
    }

    public void load() {
        File extensionFolder = new File("extensions");
        File temp = new File("tempDir");
        if (temp.listFiles() != null) {
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
            AeBot.log(Level.SEVERE, "The URL is broken", ex);
        }
        classLoader = new URLClassLoader(urls);
        for (File file : extensionFolder.listFiles()) {
            if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                String className = file.getName();
                loadClass(className);
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
                        AeBot.copyInputStream(zipFile.getInputStream(entry),
                                new BufferedOutputStream(new FileOutputStream(temp + File.separator + entry.getName())));
                    }

                } catch (IOException ex) {
                    AeBot.log(Level.SEVERE, "An error occured", ex);
                } finally {
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException ex) {
                            AeBot.log(Level.SEVERE, "An error occured", ex);
                        }
                    }
                }
            }
        }
        if (temp.listFiles() != null) {
            for (File file : temp.listFiles()) {
                if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                    String className = file.getName();
                    loadClass(className);
                }
            }
        }
    }

    public void unload() {
        for (Listener listener : listeners) {
            listener.onUnload();
        }
        listeners.clear();
        priorities.clear();
    }

    private void loadClass(String className) {
        try {
            className = className.replace("tempDir" + File.separator, "").replace("extension" + File.separator, "").replace(".class", "");
            Class cls = classLoader.loadClass(className);
            if (!Listener.class.isAssignableFrom(cls)) {
                AeBot.log(Level.SEVERE, "Class " + className + " is not a Listener");
            }
            try {
                cls.getConstructor();
            } catch (NoSuchMethodException e) {
                AeBot.log(Level.SEVERE, "Class " + className + " does not have a default constructor, cannot create instance");
                return;
            }
            Field[] declaredFields = cls.getDeclaredFields();
            boolean hasStatic = false;
            for (Field field : declaredFields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    hasStatic = true;
                    break;
                }
            }
            if (hasStatic) {
                AeBot.log(Level.WARNING, "The class " + className + " uses static references, this will break things when reloaded");
            }
            Object obj = cls.newInstance();
            if (obj instanceof Listener) {
                Listener list = (Listener) obj;
                list.onLoad();
                AeBot.log(Level.INFO, "  Added: " + list.getClass().getName());
                declarePriorities(list);
                listeners.add(list);
            }
        } catch (Throwable ex) {
            AeBot.log(Level.SEVERE, "Could not add " + className, ex);
        }
    }

    private void declarePriorities(Listener list) {
        Class thisClass = list.getClass();
        Map<EventField, EventType> priorityMap = new HashMap<>();
        try {
            Method[] methods = thisClass.getDeclaredMethods();
            for (Method method : methods) {
                EventType event = method.getAnnotation(EventType.class);
                if (event == null) {
                    continue;
                }
                AeBot.log(Level.INFO, "    Event " + event.event().name() + " was added with priority " + event.priority().name());
                priorityMap.put(event.event(), event);
            }
            priorities.put(list, priorityMap);
        } catch (SecurityException ex) {
            AeBot.log(Level.SEVERE, "Security issue", ex);
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
            for (CommandPrefix commandchar : commandChars) {
                if (event.getMessage().startsWith(commandchar.getPrefix())) {
                    if (commandchar.getOwner() != null && event.getChannel().getUsers().contains(masterBot.getUser(commandchar.getOwner()))) {
                        return;
                    }
                }
            }
            nextEvt = new CommandEvent(event);
        } else {
            nextEvt = new MessageEvent(event);
        }
        fireEvent(nextEvt);
    }

    @Override
    public void onPrivateMessage(org.pircbotx.hooks.events.PrivateMessageEvent event) throws Exception {
        Event nextEvt;
        if (isCommand(event.getMessage())) {
            nextEvt = new CommandEvent(event);
        } else {
            nextEvt = new PrivateMessageEvent(event);
        }
        fireEvent(nextEvt);
    }

    @Override
    public void onNotice(org.pircbotx.hooks.events.NoticeEvent event) throws Exception {
        Event nextEvt;
        if (isCommand(event.getMessage())) {
            nextEvt = new CommandEvent(event);
        } else {
            nextEvt = new NoticeEvent(event);
        }
        fireEvent(nextEvt);
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
        UserSnapshot user = event.getUser();
        Set<Channel> channels = user.getChannels();
        for (Channel chan : channels) {
            if (chan.getUsers().contains(masterBot.getUserBot())) {
                PartEvent partEvent = new PartEvent(user, chan);
                fireEvent(partEvent);
            }
        }
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

    @Override
    public void onKick(org.pircbotx.hooks.events.KickEvent event) throws Exception {
        KickEvent nextEvt = new KickEvent(event);
        fireEvent(nextEvt);
    }

    private boolean isCommand(String message) {
        for (CommandPrefix code : commandChars) {
            if (message.startsWith(code.getPrefix())) {
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
        synchronized (listeners) {
            for (Listener list : listeners) {
                try {
                    AeBot.log(Level.INFO, "Unloading " + list.getClass().getSimpleName());
                    list.onUnload();
                } catch (Exception e) {
                    AeBot.log(Level.SEVERE, "Error on unloading " + list.getClass().getSimpleName(), e);
                }
            }
            listeners.clear();
        }
    }

    public static List<String> getCommandPrefixes() {
        List<String> clone = new ArrayList<>();
        for (CommandPrefix prefix : commandChars) {
            clone.add(prefix.getPrefix());
        }
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
                } else {
                    EventField type = EventField.getEvent(next);
                    if (type == null) {
                        continue;
                    }
                    if (type == EventField.Permission) {
                        AeBot.getPermManager().runPermissionEvent((PermissionEvent) next);
                    } else {
                        if (type == EventField.Command) {
                            net.ae97.aebot.api.users.User user;
                            net.ae97.aebot.api.channels.Channel chan;
                            CommandEvent evt = (CommandEvent) next;
                            user = evt.getUser();
                            if (user.getNick().toLowerCase().endsWith("esper.net")) {
                                continue;
                            }
                            chan = evt.getChannel();
                            PermissionEvent permEvent = new PermissionEvent(user, chan);
                            try {
                                AeBot.getPermManager().runPermissionEvent(permEvent);
                            } catch (Exception e) {
                                AeBot.log(Level.SEVERE, "Error on permission event", e);
                                continue;
                            }
                            if (evt.getCommand().equalsIgnoreCase("reload")) {
                                User sender = evt.getUser();
                                if (sender != null) {
                                    if (!sender.hasPermission((String) null, "bot.reload")) {
                                        continue;
                                    }
                                }
                                AeBot.log(Level.INFO, "Performing a reload, please hold");
                                if (sender != null) {
                                    sender.sendNotice("Reloading");
                                }
                                unload();
                                load();
                                AeBot.log(Level.INFO, "Reloaded");
                                if (sender != null) {
                                    sender.sendNotice("Reloaded");
                                }
                                continue;
                            } else if (evt.getCommand().equalsIgnoreCase("permreload")) {
                                User sender = evt.getUser();
                                if (sender != null) {
                                    if (!sender.hasPermission((String) null, "bot.permreload")) {
                                        continue;
                                    }
                                }
                                AeBot.log(Level.INFO, "Performing a permission reload, please hold");
                                if (sender != null) {
                                    sender.sendNotice("Reloading permissions");
                                }
                                AeBot.getPermManager().reloadFile();
                                AeBot.log(Level.INFO, "Reloaded permissions");
                                if (sender != null) {
                                    sender.sendNotice("Reloaded permissions");
                                }
                                continue;
                            }
                        }
                        for (Priority prio : Priority.values()) {
                            for (Listener listener : listeners) {
                                EventType info = priorities.get(listener).get(type);
                                if (info == null) {
                                    continue;
                                }
                                if (next instanceof CancellableEvent && ((CancellableEvent) next).isCancelled() && !info.ignoreCancel()) {
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
                                                    CommandEvent evt = (CommandEvent) next;
                                                    CommandCallable call = new CommandCallable(listener, evt);
                                                    execServ.submit(call);
                                                    if (listener.getAliases() != null) {
                                                        evt.setCancelled(true);
                                                    }
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
                                            case Kick:
                                                listener.runEvent((KickEvent) next);
                                                break;
                                            case Connection:
                                                listener.runEvent((ConnectionEvent) next);
                                                break;
                                        }
                                    } catch (Exception e) {
                                        AeBot.log(Level.SEVERE, "Unhandled exception on event execution", e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AeBot.log(Level.INFO, "Ending event listener");
        }

        public void ping() {
            try {
                synchronized (this) {
                    if (this.isAlive()) {
                        this.notifyAll();
                    } else {
                    }
                }
            } catch (IllegalMonitorStateException e) {
                AeBot.log(Level.SEVERE, "Major issue on pinging event system", e);
            }
        }
    }

    private class CommandCallable implements Callable {

        private final Listener listener;
        private final CommandEvent event;

        public CommandCallable(Listener list, CommandEvent evt) {
            listener = list;
            event = evt;
        }

        @Override
        public Object call() throws Exception {
            listener.runEvent(event);
            return event;
        }
    }

    private class CommandPrefix {

        private final String prefix;
        private final String owner;

        public CommandPrefix(String p, String o) {
            prefix = p;
            owner = o;
        }

        public CommandPrefix(String p) {
            this(p, null);
        }

        public String getPrefix() {
            return prefix;
        }

        public String getOwner() {
            return owner;
        }
    }
}
