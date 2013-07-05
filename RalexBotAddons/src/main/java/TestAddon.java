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

import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.events.ActionEvent;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.KickEvent;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.api.events.NoticeEvent;
import com.lordralex.ralexbot.api.events.PartEvent;
import com.lordralex.ralexbot.api.events.PrivateMessageEvent;
import com.lordralex.ralexbot.api.events.QuitEvent;
import com.lordralex.ralexbot.settings.Settings;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class TestAddon extends Listener {

    @Override
    public void onLoad() {
        System.out.println("System: " + Settings.getGlobalSettings().getString("test"));
    }

    @Override
    public void onUnload() {
        System.out.println("Unloaded");
    }

    @Override
    @EventType(event = EventField.Action)
    public void runEvent(ActionEvent event) {
        RalexBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        RalexBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        RalexBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Kick)
    public void runEvent(KickEvent event) {
        RalexBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Message)
    public void runEvent(MessageEvent event) {
        RalexBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.NickChange)
    public void runEvent(NickChangeEvent event) {
        RalexBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Notice)
    public void runEvent(NoticeEvent event) {
        RalexBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Part)
    public void runEvent(PartEvent event) {
        RalexBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.PrivateMessage)
    public void runEvent(PrivateMessageEvent event) {
        RalexBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Quit)
    public void runEvent(QuitEvent event) {
        RalexBot.log("Event fired: " + event.toString());
    }
}
