
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
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.ae97.aebot.AeBot;
import net.ae97.aebot.api.CommandExecutor;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.sender.Sender;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class UptimeCommand extends CommandExecutor {

    @Override
    public void runEvent(CommandEvent event) {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        String uptimeString = "%D, %H, %M";
        uptimeString = uptimeString.replace("%D", TimeUnit.DAYS.convert(uptime, TimeUnit.MILLISECONDS) + " days");
        uptime -= TimeUnit.DAYS.convert(uptime, TimeUnit.MILLISECONDS) * 24 * 60 * 60 * 100;
        uptimeString = uptimeString.replace("%H", TimeUnit.HOURS.convert(uptime, TimeUnit.MILLISECONDS) + " hours");
        uptime -= TimeUnit.MINUTES.convert(uptime, TimeUnit.MILLISECONDS) * 60 * 100;
        uptimeString = uptimeString.replace("%M", TimeUnit.MINUTES.convert(uptime, TimeUnit.MILLISECONDS) + " minutes");
        Sender target = event.getChannel();
        if (target == null) {
            target = event.getChannel();
        }
        if (target == null) {
            AeBot.log(Level.INFO, "Uptime: " + uptimeString);
        } else {
            target.sendMessage("Uptime: " + uptimeString);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "uptime"
        };
    }
}
