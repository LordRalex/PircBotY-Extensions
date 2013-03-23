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
package com.lordralex.ralexbot.permissions;

import com.lordralex.ralexbot.settings.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lord_Ralex
 * @version 1.0
 */
public class PermissionGroup {

    protected final List<String> perms = new ArrayList<>();
    protected final String name;

    public PermissionGroup(String name) {
        this.name = name;
        load();
    }

    public final void load() {
        perms.clear();
        Settings permFile = new Settings(new File("permissions", "groups.yml"));
        List<String> temp = permFile.getStringList(name);
        if (temp != null) {
            perms.addAll(temp);
        }
    }

    public List<String> getPerms() {
        return new ArrayList<>(perms);
    }
}
