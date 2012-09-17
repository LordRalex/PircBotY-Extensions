/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.api;

import com.lordralex.ralexbot.api.events.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Listener {

    public final Map<EventField, Priority> priorities = new HashMap<>();

    public abstract void setup();

    public final void declareValues(Class thisClass) {
        try {
            Method[] methods = thisClass.getDeclaredMethods();
            for (Method method : methods) {
                EventType event = method.getAnnotation(EventType.class);
                if (event == null) {
                    continue;
                }
                EventField eventType = event.event();
                Priority prio = event.priority();
                priorities.put(eventType, prio);
            }
        } catch (SecurityException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void runEvent(CommandEvent event) {
    }

    public void runEvent(MessageEvent event) {
    }

    public void runEvent(JoinEvent event) {
    }

    public void runEvent(NoticeEvent event) {
    }

    public void runEvent(PartEvent event) {
    }

    public void runEvent(PrivateMessageEvent event) {
    }

    public void runEvent(QuitEvent event) {
    }

    public void runEvent(NickChangeEvent event) {
    }
}
