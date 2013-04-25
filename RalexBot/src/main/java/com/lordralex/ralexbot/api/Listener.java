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
package com.lordralex.ralexbot.api;

import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.events.*;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

public abstract class Listener {

    public final Map<EventField, EventType> priorities = new EnumMap<>(EventField.class);

    public void setup() {
    }

    public final void declareValues(Class thisClass) {
        try {
            Method[] methods = thisClass.getDeclaredMethods();
            for (Method method : methods) {
                EventType event = method.getAnnotation(EventType.class);
                if (event == null) {
                    continue;
                }
                System.out.println("    *Event " + event.event().name() + " was added with priority " + event.priority().name());
                priorities.put(event.event(), event);
            }
        } catch (SecurityException ex) {
            RalexBot.getLogger().log(Level.SEVERE, "Security issue", ex);
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

    public void runEvent(ActionEvent event) {
    }

    public String[] getAliases() {
        return new String[0];
    }
}
