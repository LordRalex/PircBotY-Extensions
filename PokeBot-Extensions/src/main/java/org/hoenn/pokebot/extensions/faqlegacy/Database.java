/*
 * Copyright (C) 2014 Lord_Ralex
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
package org.hoenn.pokebot.extensions.faqlegacy;

import java.io.IOException;

/**
 *
 * @author Lord_Ralex
 */
public abstract class Database {

    private String master = null;
    private boolean readOnly = false;
    private final String name;
    private boolean override = false;

    public Database(String n) {
        name = n;
    }

    public void load() throws IOException {
    }

    public String getName() {
        return name;
    }

    public void setMaster(String m) {
        master = m;
    }

    public String getMaster() {
        if (override) {
            return null;
        }
        return master;
    }

    public void setOverride(boolean newState) {
        override = newState;
    }

    public boolean getOverride() {
        return override;
    }

    public void setReadonly(boolean newBool) {
        readOnly = newBool;
    }

    public boolean isReadonly() {
        return readOnly;
    }

    public abstract String[] getEntry(String key);

}
