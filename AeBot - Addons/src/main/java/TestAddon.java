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

import net.ae97.aebot.AeBot;
import net.ae97.aebot.api.EventField;
import net.ae97.aebot.api.EventType;
import net.ae97.aebot.api.Listener;
import net.ae97.aebot.api.events.ActionEvent;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.events.JoinEvent;
import net.ae97.aebot.api.events.KickEvent;
import net.ae97.aebot.api.events.MessageEvent;
import net.ae97.aebot.api.events.NickChangeEvent;
import net.ae97.aebot.api.events.NoticeEvent;
import net.ae97.aebot.api.events.PartEvent;
import net.ae97.aebot.api.events.PrivateMessageEvent;
import net.ae97.aebot.api.events.QuitEvent;
import net.ae97.aebot.permissions.Permission;
import net.ae97.aebot.settings.Settings;
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
        AeBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        AeBot.log("Event fired: " + event.toString());
        Map<String, Set<Permission>> perms = event.getUser().getPermissions();
        for (String key : perms.keySet()) {
            AeBot.log("Perms in " + key);
            for (Permission perm : perms.get(key)) {
                AeBot.log(" -" + perm.getName());
            }
        }
        AeBot.log("Does " + event.getUser().getNick() + " have the permission in " + event.getChannel().getName() + ": " + event.getUser().hasPermission(event.getChannel().getName(), "permission.test"));
    }

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        AeBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Kick)
    public void runEvent(KickEvent event) {
        AeBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Message)
    public void runEvent(MessageEvent event) {
        AeBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.NickChange)
    public void runEvent(NickChangeEvent event) {
        AeBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Notice)
    public void runEvent(NoticeEvent event) {
        AeBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Part)
    public void runEvent(PartEvent event) {
        AeBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.PrivateMessage)
    public void runEvent(PrivateMessageEvent event) {
        AeBot.log("Event fired: " + event.toString());
    }

    @Override
    @EventType(event = EventField.Quit)
    public void runEvent(QuitEvent event) {
        AeBot.log("Event fired: " + event.toString());
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "test"
        };
    }
}
