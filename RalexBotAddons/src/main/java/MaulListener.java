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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.ae97.ralexbot.api.EventField;
import net.ae97.ralexbot.api.EventType;
import net.ae97.ralexbot.api.Listener;
import net.ae97.ralexbot.api.channels.Channel;
import net.ae97.ralexbot.api.events.MessageEvent;
import net.ae97.ralexbot.api.users.BotUser;
import net.ae97.ralexbot.api.users.User;
import net.ae97.ralexbot.settings.Settings;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class MaulListener extends Listener {

    private int delay;
    private final List<String> channels = new ArrayList<>();
    private final List<String> replies = new ArrayList<>();
    private volatile ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final Set<String> ignoreList = new HashSet<>();
    private final Set<String> moves = new HashSet<>();

    @Override
    public void onLoad() {
        Settings settings = new Settings(new File("settings", "maul.yml"));
        List<String> chans = settings.getStringList("channels");
        delay = settings.getInt("delay");
        if (delay == 0) {
            delay = 2000;
        }
        if (chans != null) {
            channels.addAll(chans);
        }
        List<String> r = settings.getStringList("replies");
        if (r != null) {
            replies.addAll(r);
        }
        List<String> moveList = settings.getStringList("moves");
        if (moveList != null) {
            moves.addAll(moveList);
        }
    }

    @Override
    public void onUnload() {
        service.shutdownNow();
    }

    @Override
    @EventType(event = EventField.Message)
    public void runEvent(MessageEvent event) {
        if (!event.getMessage().startsWith("Absol,")) {
            return;
        }
        if (ignoreList.contains(event.getUser().getIP())) {
            return;
        }
        if (!channels.contains(event.getChannel().getName())) {
            return;
        }
        String nick = BotUser.getBotUser().getNick();
        if (event.getMessage().startsWith(nick + ", maul")) {
            String target = event.getMessage().substring((nick + ", maul").length()).trim();
            if (service != null) {
                synchronized (service) {
                    service.schedule(new LookThread(event.getChannel(), target), delay, TimeUnit.MILLISECONDS);
                    service.schedule(new RunThread(event.getChannel(), target, event.getUser().getNick()), delay * 2, TimeUnit.MILLISECONDS);
                }
            }
        } else if (event.getMessage().startsWith(nick + ", return")) {
            if (service != null) {
                event.getUser().sendNotice("Returning");
                service.shutdownNow();
                service = null;
            }
        } else if (event.getMessage().equalsIgnoreCase(nick + ", I choose you") && event.getUser().isVerified().equals("Lord_Ralex")) {
            if (service != null && !service.isShutdown()) {
                service.shutdownNow();
            }
            service = Executors.newSingleThreadScheduledExecutor();
        } else if (event.getMessage().startsWith(nick + ", ignnore")) {
            String[] param = event.getMessage().split(" ");
            if (param.length != 3) {
                return;
            }
            String name = param[2];
            synchronized (ignoreList) {
                ignoreList.add(User.getUser(name).getIP());
            }
        } else if (event.getMessage().startsWith(nick + ", listen to")) {
            String[] param = event.getMessage().split(" ");
            if (param.length != 4) {
                return;
            }
            String name = param[3];
            synchronized (ignoreList) {
                ignoreList.remove(User.getUser(name).getIP());
            }
        } else if (event.getMessage().startsWith(nick + ", attack")) {
            String target = event.getMessage().substring((nick + ", attack").length()).trim();
            if (service != null) {
                synchronized (service) {
                    service.schedule(new AttackThread(event.getChannel(), target), delay * 2, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    private class RunThread implements Runnable {

        private final Channel chan;
        private final String target;
        private final String orig;

        public RunThread(Channel c, String t, String o) {
            chan = c;
            target = t;
            orig = o;
        }

        @Override
        public void run() {
            synchronized (ignoreList) {
                if (ignoreList.contains(User.getUser(target).getIP())) {
                    return;
                }
            }
            Random ran = new Random();
            User t;
            String addons = "";
            if (ran.nextInt(1000) > 900) {
                String[] more = orig.split(" ");
                t = User.getUser(more[0].trim());
                if (more.length > 1) {
                    for (int i = 1; i < more.length; i++) {
                        addons += " " + more[i];
                    }
                }
            } else {
                String[] more = target.split(" ");
                t = User.getUser(more[0].trim());
                if (more.length > 1) {
                    for (int i = 1; i < more.length; i++) {
                        addons += " " + more[i];
                    }
                }
            }
            String message = replies.get(ran.nextInt(replies.size()));
            message = message.replace("{user}", t.getNick());
            message += addons;
            BotUser.getBotUser().sendAction(chan.getName(), message.trim());
        }
    }

    private class LookThread implements Runnable {

        private final Channel chan;
        private final String name;

        public LookThread(Channel c, String n) {
            chan = c;
            name = n;
        }

        @Override
        public void run() {
            synchronized (ignoreList) {
                if (ignoreList.contains(User.getUser(name).getIP())) {
                    return;
                }
            }
            BotUser.getBotUser().sendAction(chan.getName(), "looks at " + name.split(" ")[0].trim());
        }
    }

    private class AttackThread implements Runnable {

        private final Channel channel;
        private final String target;

        public AttackThread(Channel chan, String tar) {
            channel = chan;
            target = tar;
        }

        @Override
        public void run() {
            BotUser.getBotUser().sendAction(channel.getName(), "uses " + moves.toArray(new String[moves.size()][new Random().nextInt(moves.size())]) + " on " + target);
        }
    }
}
