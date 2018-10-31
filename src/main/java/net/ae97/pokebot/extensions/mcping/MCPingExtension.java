/*
 * Copyright (C) 2016 Joshua
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
package net.ae97.pokebot.extensions.mcping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;
import net.ae97.pokebot.extension.ExtensionLoadFailedException;

/**
 *
 * @author Joshua
 */
public class MCPingExtension extends Extension implements CommandExecutor {
    public static final String BLACKLISTED_MESSAGE = "Server is blacklisted";


    @Override
    public void load() throws ExtensionLoadFailedException {
        PokeBot.getEventHandler().registerCommandExecutor(this);
    }
    
    @Override
    public void runEvent(CommandEvent ce) {
        if (ce.getArgs().length != 1) {
            ce.respond("Usage: mcping <server ip>[:port]");
            return;
        }
        try {
            BlacklistChecker.refreshBlacklist();
            if(BlacklistChecker.isHostBlacklisted(ce.getArgs()[0])) {
                ce.respond(BLACKLISTED_MESSAGE);
            }
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Error updating server blacklist", e);
        }
        try {
            Process pinger = new ProcessBuilder().command("python", "bin/mcping.py", ce.getArgs()[0]).start();
            pinger.waitFor();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(pinger.getInputStream()))) {
                ce.respond(reader.readLine());
            }
        } catch (IOException | InterruptedException ex) {
            ce.respond("Error pinging server: " + ex.getMessage());
            getLogger().log(Level.WARNING, "Error pinging server", ex);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "mcping"
        };
    }

    @Override
    public String getName() {
        return "mcping";
    }

}
