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
package net.ae97.pokebot.extensions.htop;

import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;

/**
 * @author Lord_Ralex
 */
public class HtopExtension extends Extension implements CommandExecutor {

    @Override
    public String getName() {
        return "Htop";
    }

    @Override
    public void load() {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        Runtime runtime = Runtime.getRuntime();
        long free, total, max;
        int perTotal, perMax;
        int proc = runtime.availableProcessors();
        free = runtime.freeMemory();
        total = runtime.totalMemory();
        max = runtime.maxMemory();
        perTotal = (int) (((total - free) / (double) total) * 100);
        perMax = (int) ((total / (double) max) * 100);
        event.respond("Memory: "
                + convert(total - free) + "/" + convert(total) + " (" + perTotal + "%)"
                + ", max allowed: " + convert(max) + " (" + perMax + "%)");

    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "htop"
        };
    }

    private String convert(long value) {
        int multiple = 0;
        while (value / 1024 > 0) {
            value = value / 1024;
            multiple++;
        }
        int remains = (int) value % 1024;
        String result = remains + "B";
        switch (multiple) {
            case 0: {
                result = remains + "B";
            }
            break;
            case 1: {
                result = remains + "KB";
            }
            break;
            case 2: {
                result = remains + "MB";
            }
            break;
            case 3: {
                result = remains + "GB";
            }
            break;
            case 4: {
                result = remains + "TB";
            }
            break;
        }

        return result;
    }

}
