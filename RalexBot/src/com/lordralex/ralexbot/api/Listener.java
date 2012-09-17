/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.api;

import com.lordralex.ralexbot.api.events.MessageEvent;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.PircBotX;

public abstract class Listener {

    Map<EventField, Priority> priorities = new HashMap<>();

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

    @EventType(event = EventField.Message)
    public void onMessage(MessageEvent event) {
    }
}
