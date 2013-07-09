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

import net.ae97.ralexbot.RalexBot;
import net.ae97.ralexbot.api.EventField;
import net.ae97.ralexbot.api.EventType;
import net.ae97.ralexbot.api.Listener;
import net.ae97.ralexbot.api.events.ActionEvent;
import net.ae97.ralexbot.api.events.CommandEvent;
import net.ae97.ralexbot.api.events.JoinEvent;
import net.ae97.ralexbot.api.events.KickEvent;
import net.ae97.ralexbot.api.events.MessageEvent;
import net.ae97.ralexbot.api.events.NickChangeEvent;
import net.ae97.ralexbot.api.events.NoticeEvent;
import net.ae97.ralexbot.api.events.PartEvent;
import net.ae97.ralexbot.api.events.PrivateMessageEvent;
import net.ae97.ralexbot.api.events.QuitEvent;
import net.ae97.ralexbot.permissions.Permission;
import net.ae97.ralexbot.settings.Settings;
import java.util.Map;
import java.util.Set;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class TestAddon extends Listener {

    @Override
    public void onLoad() {
    }

    @Override
    public void onUnload() {
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
        Map<String, Set<Permission>> perms = event.getUser().getPermissions();
        for (String key : perms.keySet()) {
            RalexBot.log("Perms in " + key);
            for (Permission perm : perms.get(key)) {
                RalexBot.log(" -" + perm.getName());
            }
        }
        RalexBot.log("Does " + event.getUser().getNick() + " have the permission in " + event.getChannel().getName() + ": " + event.getUser().hasPermission(event.getChannel().getName(), "permission.test"));
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

    @Override
    public String[] getAliases() {
        return new String[]{
            "test"
        };
    }
}
