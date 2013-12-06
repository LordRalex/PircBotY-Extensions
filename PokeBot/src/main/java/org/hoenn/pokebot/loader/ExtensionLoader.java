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
package org.hoenn.pokebot.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import org.hoenn.pokebot.extension.Extension;

/**
 * @author Lord_Ralex
 */
public class ExtensionLoader extends URLClassLoader {

    private final ExtensionPluginLoader loader;
    private final Map<String, Class<?>> classes = new HashMap<>();

    ExtensionLoader(ExtensionPluginLoader l, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        loader = l;
    }

    protected Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
        Class<?> result = classes.get(name);
        if (result == null) {
            if (checkGlobal) {
                result = loader.getClassByName(name);
            }
            if (result == null) {
                result = Class.forName(name, true, this);
            }
            if (result != null) {
                loader.setClass(name, result);
            }
        }
        if (result != null) {
            classes.put(name, result);
        }
        return result;
    }

    public Class<? extends Extension> findMainClass() {
        for (Class<?> cl : classes.values()) {
            if (Extension.class.isAssignableFrom(cl)) {
                return cl.asSubclass(Extension.class);
            }
        }
        return null;
    }
}
