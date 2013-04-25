/*
 * Copyright (C) 2013 Laptop
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
package com.lordralex.ralexbot.api.users;

import com.lordralex.ralexbot.api.sender.Sender;
import com.lordralex.ralexbot.permissions.Permissible;
import com.lordralex.ralexbot.permissions.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.pircbotx.Channel;

/**
 * @version 1.0
 * @author Laptop
 */
public class UserSnapshot implements Sender, Permissible {

    private final org.pircbotx.UserSnapshot snapshot;

    public UserSnapshot(org.pircbotx.UserSnapshot snap) {
        snapshot = snap;
    }

    @Override
    public void sendMessage(String message) {
        snapshot.sendMessage(message);
    }

    @Override
    public void sendNotice(String message) {
        new BotUser().sendNotice(snapshot.getNick(), message);
    }

    @Override
    public void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public void sendNotice(String[] messages) {
        for (String message : messages) {
            sendNotice(message);
        }
    }

    public String[] getChannels() {
        Set<Channel> channels = snapshot.getChannels();
        List<String> names = new ArrayList<>();
        for (Channel chan : channels) {
            names.add(chan.getName());
        }
        return names.toArray(new String[names.size()]);
    }

    public String getName() {
        return snapshot.getNick();
    }

    @Override
    public boolean hasPermission(Permission perm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPermission(Permission perm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPermission(Permission perm, boolean val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePermission(Permission perm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Permission, Boolean> getPermissions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
