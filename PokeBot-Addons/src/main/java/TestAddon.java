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

import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.EventType;
import org.hoenn.pokebot.api.Listener;
import org.hoenn.pokebot.api.events.ActionEvent;
import org.hoenn.pokebot.api.events.CommandEvent;
import org.hoenn.pokebot.api.events.JoinEvent;
import org.hoenn.pokebot.api.events.KickEvent;
import org.hoenn.pokebot.api.events.MessageEvent;
import org.hoenn.pokebot.api.events.NickChangeEvent;
import org.hoenn.pokebot.api.events.NoticeEvent;
import org.hoenn.pokebot.api.events.PartEvent;
import org.hoenn.pokebot.api.events.PrivateMessageEvent;
import org.hoenn.pokebot.api.events.QuitEvent;
import org.hoenn.pokebot.permissions.Permission;
import java.util.Map;
import java.util.Set;
import org.hoenn.pokebot.api.CommandExecutor;

/**
 * @author Lord_Ralex
 */
public class TestAddon implements CommandExecutor, Listener {

    @EventType
    public void runEvent(ActionEvent event) {
        PokeBot.log("Event fired: " + event.toString());
    }

    @Override
    public void runEvent(CommandEvent event) {
        PokeBot.log("Event fired: " + event.toString());
        Map<String, Set<Permission>> perms = event.getUser().getPermissions();
        for (String key : perms.keySet()) {
            PokeBot.log("Perms in " + key);
            for (Permission perm : perms.get(key)) {
                PokeBot.log(" -" + perm.getName());
            }
        }
        PokeBot.log("Does " + event.getUser().getNick() + " have the permission in " + event.getChannel().getName() + ": " + event.getUser().hasPermission(event.getChannel().getName(), "permission.test"));
    }

    @EventType
    public void runEvent(JoinEvent event) {
        PokeBot.log("Event fired: " + event.toString());
    }

    @EventType
    public void runEvent(KickEvent event) {
        PokeBot.log("Event fired: " + event.toString());
    }

    @EventType
    public void runEvent(MessageEvent event) {
        PokeBot.log("Event fired: " + event.toString());
    }

    @EventType
    public void runEvent(NickChangeEvent event) {
        PokeBot.log("Event fired: " + event.toString());
    }

    @EventType
    public void runEvent(NoticeEvent event) {
        PokeBot.log("Event fired: " + event.toString());
    }

    @EventType
    public void runEvent(PartEvent event) {
        PokeBot.log("Event fired: " + event.toString());
    }

    @EventType
    public void runEvent(PrivateMessageEvent event) {
        PokeBot.log("Event fired: " + event.toString());
    }

    @EventType
    public void runEvent(QuitEvent event) {
        PokeBot.log("Event fired: " + event.toString());
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "test"
        };
    }
}
