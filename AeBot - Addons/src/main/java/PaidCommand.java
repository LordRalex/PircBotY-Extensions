
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
import net.ae97.aebot.AeBot;
import net.ae97.aebot.api.EventField;
import net.ae97.aebot.api.EventType;
import net.ae97.aebot.api.Listener;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.sender.Sender;
import net.ae97.aebot.settings.Settings;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class PaidCommand extends Listener {

    private final String HASPAID = "https://minecraft.net/haspaid.jsp?user={0}";
    private int MAX_NAMES;
    private final ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    public void onLoad() {
        MAX_NAMES = new Settings(new File("settings", "paid.yml")).getInt("name-limit");
    }

    @Override
    public void onUnload() {
        es.shutdown();
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.getArgs().length == 0) {
            return;
        }
        if (event.getArgs().length > MAX_NAMES) {
            event.getUser().sendNotice("I can only do " + MAX_NAMES + " lookups at once");
            return;
        }
        for (String name : event.getArgs()) {
            Sender target = event.getChannel();
            if (target == null) {
                target = event.getUser();
            }
            es.submit(new Lookup(target, name));
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "paid"
        };
    }

    private class Lookup implements Runnable {

        private String name;
        private Sender target;

        public Lookup(Sender tar, String n) {
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
                    target.sendMessage("The user '" + name + "' is a premium account");
                } else {
                    target.sendMessage("The user '" + name + "' is NOT a premium account");
                }
            } catch (Exception e) {
                AeBot.logSevere("An error occured on looking up " + name, e);
                target.sendMessage("An error occured while looking to see if '" + name + "' has paid");
            }
        }
    }
}
