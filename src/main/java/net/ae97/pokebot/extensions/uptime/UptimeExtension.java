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
package net.ae97.pokebot.extensions.uptime;

import java.lang.management.ManagementFactory;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;

/**
 * @author Lord_Ralex
 */
public class UptimeExtension extends Extension implements CommandExecutor {

    @Override
    public String getName() {
        return "Uptime";
    }

    @Override
    public void load() {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000 / 60;
        String uptimeString = "%D, %H, %M";
        uptimeString = uptimeString.replace("%D", (uptime / 60 / 24) + " days");
        uptimeString = uptimeString.replace("%H", (uptime / 60 % 24) + " hours");
        uptimeString = uptimeString.replace("%M", (uptime % 60 % 60) + " minutes");
        event.respond("Uptime: " + uptimeString);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "uptime"
        };
    }
}
