package com.lordralex.ralexbot.api.apihandlers;

import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.events.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.hooks.ListenerAdapter;

/**
 * @version 1.0
 * @author Joshua
 */
public final class EventManager extends ListenerAdapter {

    List<Listener> listeners = new ArrayList<Listener>();
    Scheduler scheduler;

    public EventManager() {
        listeners = new ArrayList<Listener>();
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
                            list.declarePriorities();
                            listeners.add(list);
                            System.out.println("  Added: " + list.getClass().getName());
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        scheduler = new Scheduler(listeners);
    }

    public void runEvent(Event eventToRun) {
        scheduler.scheduleEvent(eventToRun);
    }

    @Override
    public void onJoin(org.pircbotx.hooks.events.JoinEvent event) throws Exception {
        JoinEvent evt = new JoinEvent(event.getChannel().getName(), event.getUser().getNick(), event.getUser().getLogin(), event.getUser().getHostmask());
        runEvent(evt);
    }

    @Override
    public void onMessage(org.pircbotx.hooks.events.MessageEvent event) throws Exception {

        String channel = event.getChannel().getName();
        String message = event.getMessage();
        String sender = event.getUser().getNick();
        String hostname = event.getUser().getHostmask();
        String login = event.getUser().getLogin();

        if (message.startsWith("*") || message.startsWith("$")) {
            String[] parts = message.split(" ");
            String command = parts[0].substring(1, parts[0].length());
            String[] args = new String[parts.length - 1];
            for (int i = 0; i < args.length; i++) {
                args[i] = parts[i + 1];
            }

            CommandEvent evt = new CommandEvent(command, sender, channel, args);
            runEvent(evt);
        } else {
            MessageEvent evt = new MessageEvent(channel, sender, login, hostname, message);
            runEvent(evt);
        }
    }

    @Override
    public void onPrivateMessage(org.pircbotx.hooks.events.PrivateMessageEvent event) throws Exception {
        String message = event.getMessage();
        String sender = event.getUser().getNick();
        String hostname = event.getUser().getHostmask();
        String login = event.getUser().getLogin();

        if (message.startsWith("*") || message.startsWith("$")) {
            String[] parts = message.split(" ");
            String command = parts[0].substring(1, parts[0].length());
            String[] args = new String[parts.length - 1];
            for (int i = 0; i < args.length; i++) {
                args[i] = parts[i + 1];
            }

            CommandEvent evt = new CommandEvent(command, sender, null, args);
            runEvent(evt);
        } else {
            PrivateMessageEvent evt = new PrivateMessageEvent(sender, login, hostname, message);
            runEvent(evt);
        }
    }

    @Override
    public void onNickChange(org.pircbotx.hooks.events.NickChangeEvent event) throws Exception {
        NickChangeEvent evt = new NickChangeEvent(event.getOldNick(), event.getUser().getLogin(), event.getUser().getHostmask(), event.getNewNick());
        runEvent(evt);
    }

    @Override
    public void onPart(org.pircbotx.hooks.events.PartEvent event) throws Exception {
        PartEvent evt = new PartEvent(event.getChannel().getName(), event.getUser().getNick(), event.getUser().getLogin(), event.getUser().getHostmask());;
        runEvent(evt);
    }

    @Override
    public void onQuit(org.pircbotx.hooks.events.QuitEvent event) throws Exception {
        QuitEvent evt = new QuitEvent(event.getUser().getNick(), event.getUser().getLogin(), event.getUser().getHostmask(), event.getReason());
        runEvent(evt);
    }
}
