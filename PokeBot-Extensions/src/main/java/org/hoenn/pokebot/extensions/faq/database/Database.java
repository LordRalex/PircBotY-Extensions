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
package org.hoenn.pokebot.extensions.faq.database;

import java.util.Map;

/**
 * @author Lord_Ralex
 */
public abstract class Database {

    protected final String name;
    protected final Map<String, Object> parameters;

    public Database(String n, Map<String, Object> params) {
        name = n;
        parameters = params;
    }

    public abstract void load();

    public String getName() {
        return name;
    }

    public abstract String[] getEntry(String[] key);
}
