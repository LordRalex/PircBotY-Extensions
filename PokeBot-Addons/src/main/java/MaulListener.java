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
import org.hoenn.pokebot.api.EventType;
import org.hoenn.pokebot.api.Listener;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.events.MessageEvent;
import org.hoenn.pokebot.api.users.BotUser;
import org.hoenn.pokebot.api.users.User;
import org.hoenn.pokebot.settings.Settings;

/**
 * @author Lord_Ralex
 */
public class MaulListener implements Listener {

    private final int delay;
    private final List<String> channels = new ArrayList<>();
    private final List<String> replies = new ArrayList<>();
    private volatile ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final Set<String> ignoreList = new HashSet<>();
    private final List<String> moves = new ArrayList<>();

    public MaulListener() {
        Settings settings = new Settings(new File("settings", "maul.yml"));
        List<String> chans = settings.getStringList("channels");
        delay = settings.getInt("delay") == 0 ? 2000 : settings.getInt("delay");
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

    @EventType
    public void runEvent(MessageEvent event) {
        if (!event.getMessage().startsWith(BotUser.getBotUser().getNick() + ",")) {
            return;
        }
        if (ignoreList.contains(event.getUser().getIP())) {
            if (!event.getUser().hasPermission(event.getChannel(), "maul.alwaysListen")) {
                return;
            }
        }
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        String nick = BotUser.getBotUser().getNick();
        if (event.getMessage().startsWith(nick + ", maul")) {
            String target = event.getMessage().substring((nick + ", maul").length()).trim();
            if (target.equalsIgnoreCase(nick)) {
                target = event.getUser().getNick();
            }
            if (service != null) {
                synchronized (service) {
                    service.schedule(new LookThread(event.getChannel(), target), delay, TimeUnit.MILLISECONDS);
                    service.schedule(new RunThread(event.getChannel(), target, event.getUser().getNick()), delay * 2, TimeUnit.MILLISECONDS);
                }
            }
        } else if (event.getMessage().startsWith(nick + ", return") && event.getUser().hasPermission(event.getChannel(), "maul.return")) {
            if (service != null) {
                event.getUser().sendNotice("Returning");
                service.shutdownNow();
            }
        } else if (event.getMessage().equalsIgnoreCase(nick + ", I choose you") && event.getUser().hasPermission(event.getChannel(), "maul.choose")) {
            if (service != null && !service.isShutdown()) {
                service.shutdownNow();
            }
            service = Executors.newSingleThreadScheduledExecutor();
        } else if (event.getMessage().startsWith(nick + ", ignore") && event.getUser().hasPermission(event.getChannel(), "maul.ignore")) {
            String[] param = event.getMessage().split(" ");
            if (param.length != 3) {
                return;
            }
            String name = param[2];
            synchronized (ignoreList) {
                if (ignoreList.contains(event.getUser().getIP())) {
                    return;
                }
                ignoreList.add(User.getUser(name).getIP());
            }
        } else if (event.getMessage().startsWith(nick + ", listen to") && event.getUser().hasPermission(event.getChannel(), "maul.listen")) {
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
            if (target.equalsIgnoreCase(nick)) {
                target = event.getUser().getNick();
            }
            if (service != null) {
                synchronized (service) {
                    service.schedule(new AttackThread(event.getChannel(), target), delay, TimeUnit.MILLISECONDS);
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
            String effective;
            switch (new Random().nextInt(5)) {
                case 0:
                    effective = "";
                    break;
                case 1:
                    effective = "It was somewhat effective.";
                    break;
                case 2:
                    effective = "It was super effective!";
                    break;
                case 3:
                    effective = "It was not that effective...";
                    break;
                case 4:
                    effective = "It missed";
                    break;
                default:
                    effective = "";
            }
            BotUser.getBotUser().sendAction(channel.getName(), "uses " + moves.get(new Random().nextInt(moves.size())) + " on " + target + ". " + effective);
        }
    }
}
