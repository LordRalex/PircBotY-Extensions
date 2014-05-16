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
package net.ae97.pokebot.extensions.paid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import net.ae97.pircboty.api.Event;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;

/**
 * @author Lord_Ralex
 */
public class PaidExecutor extends Extension implements CommandExecutor {

    private final String HASPAID = "https://minecraft.net/haspaid.jsp?user={0}";
    private final ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    public String getName() {
        return "Paid";
    }

    @Override
    public void load() {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(final CommandEvent event) {
        if (event.getArgs().length == 0) {
            return;
        }
        if (event.getArgs().length > getConfig().getInt("name-limit", 3)) {
            event.getUser().send().notice("I can only do " + getConfig().getInt("name-limit", 3) + " lookups at once");
            return;
        }
        for (String name : event.getArgs()) {
            if (name.endsWith(",")) {
                name = name.substring(0, name.length() - 2);
            }
            es.submit(new Lookup(event, name));
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "paid"
        };
    }

    private class Lookup implements Runnable {

        private final String name;
        private final Event target;

        public Lookup(Event tar, String n) {
            target = tar;
            name = n;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(HASPAID.replace("{0}", name));
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String reply = reader.readLine();
                if (reply.equalsIgnoreCase("true")) {
                    target.respond("The user '" + name + "' is a premium account");
                } else {
                    target.respond("The user '" + name + "' is NOT a premium account");
                }
            } catch (IOException e) {
                PokeBot.getLogger().log(Level.SEVERE, "An error occured on looking up " + name, e);
                target.respond("An error occured while looking to see if '" + name + "' has paid");
            }
        }
    }
}
