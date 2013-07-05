
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
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.settings.Settings;
import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class BanManager extends Listener {

    private Settings banSaves;
    private final Set<Ban> banList = new TreeSet<>(new BanComparator());
    private final ScheduledExecutorService srv = Executors.newSingleThreadScheduledExecutor();
    private final UnbanRunnable unbanRunnable = new UnbanRunnable();

    @Override
    public void onLoad() {
        banSaves = new Settings(new File("bans.yml"));
        if (!banList.isEmpty()) {
            Ban ban = banList.iterator().next();
            long initDelay = ban.getUnbanTime() - System.currentTimeMillis();
            srv.schedule(unbanRunnable, initDelay, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onUnload() {
        banSaves.save();
        srv.shutdownNow();
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "tempban",
            "tb"
        };
    }

    private long parseTime(String time) {
        int banTime = 0;
        String[] args = time.split(",");
        for (String arg : args) {
            String arg0 = arg.split(" ")[1].trim().toLowerCase();
            int count = Integer.parseInt(arg.split(" ")[0].trim());
            if (arg0.startsWith("month")) {
                banTime += (60 * 60 * 24 * 30) * count;
            } else if (arg0.startsWith("week")) {
                banTime += (60 * 60 * 24 * 7) * count;
            } else if (arg0.startsWith("day")) {
                banTime += (60 * 60 * 24) * count;
            } else if (arg0.startsWith("hour")) {
                banTime += (60 * 60) * count;
            } else if (arg0.startsWith("minute")) {
                banTime += (60) * count;
            }
        }
        banTime *= 1000;
        return banTime;
    }

    private void saveBans() {
    }

    private class Ban implements Serializable {

        private final long unbanTime;
        private final String unbanString;
        private final String channel;

        public Ban(long time, String str, String chan) {
            unbanTime = time;
            unbanString = str;
            channel = chan;
        }

        long getUnbanTime() {
            return unbanTime;
        }

        String getUnbanString() {
            return unbanString;
        }

        String getChannel() {
            return channel;
        }
    }

    private class BanComparator implements Comparator<Ban> {

        @Override
        public int compare(Ban o1, Ban o2) {
            if (o1.getUnbanTime() > o2.getUnbanTime()) {
                return 1;
            } else if (o1.getUnbanTime() < o2.getUnbanTime()) {
                return 0;
            }
            int i = o1.getChannel().compareTo(o2.getChannel());
            if (i == 0) {
                return o1.getUnbanString().compareTo(o2.getUnbanString());
            } else {
                return i;
            }
        }
    }

    private class UnbanRunnable implements Runnable {

        @Override
        public void run() {
            synchronized (banList) {
                if (banList.isEmpty()) {
                    return;
                }
                Ban ban = banList.iterator().next();
                if (ban.getUnbanTime() > System.currentTimeMillis()) {
                    scheduleLater();
                    return;
                }
            }
        }

        private void scheduleLater() {
            if (!banList.isEmpty()) {
                Ban ban = banList.iterator().next();
                long initDelay = ban.getUnbanTime() - System.currentTimeMillis();
                srv.schedule(this, initDelay, TimeUnit.MILLISECONDS);
            }
        }
    }
}
