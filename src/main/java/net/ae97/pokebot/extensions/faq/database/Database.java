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
package net.ae97.pokebot.extensions.faq.database;

import java.util.Map;

/**
 * @author Lord_Ralex
 */
public abstract class Database {

    private final String name;
    private final Map<String, String> parameters;
    private final String owner;

    public Database(String n, Map<String, String> params) {
        name = n;
        parameters = params;
        owner = parameters.get("owner");
    }

    public abstract void load();

    public String getName() {
        return name;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getOwner() {
        return owner;
    }

    public abstract String[] getEntry(String[] key);
}
