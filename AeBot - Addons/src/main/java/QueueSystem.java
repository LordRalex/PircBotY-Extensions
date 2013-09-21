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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import net.ae97.aebot.api.CommandExecutor;
import net.ae97.aebot.api.EventType;
import net.ae97.aebot.api.Listener;
import net.ae97.aebot.api.channels.Channel;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.events.JoinEvent;
import net.ae97.aebot.api.events.KickEvent;
import net.ae97.aebot.api.events.PartEvent;
import net.ae97.aebot.api.events.QuitEvent;
import net.ae97.aebot.api.users.User;
import net.ae97.aebot.settings.Settings;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class QueueSystem extends CommandExecutor implements Listener {

    private final Map<Channel, Queue<User>> queueMap = new ConcurrentHashMap<>();
    private final Set<Channel> activeChans = new HashSet<>();
    private final Map<Channel, Set<User>> gettingHelped = new ConcurrentHashMap<>();
    private final Set<String> channelsToUseIn = new HashSet<>();
    private final Set<String> loginNames = new HashSet<>();
    private Settings settings;

    public QueueSystem() {
        queueMap.clear();
        activeChans.clear();
        channelsToUseIn.clear();
        settings = new Settings(new File("settings", "queue.yml"));
        List<String> ch = settings.getStringList("channels");
        if (ch != null) {
            channelsToUseIn.addAll(ch);
        }
        for (String channel : channelsToUseIn) {
            Channel chan = Channel.getChannel(channel);
            queueMap.put(chan, new LinkedBlockingQueue<User>());
            gettingHelped.put(chan, new HashSet<User>());
        }
        List<String> names = settings.getStringList("login-names");
        if (names != null) {
            loginNames.addAll(names);
        }
    }

    public void onUnload() {
        for (Channel key : gettingHelped.keySet()) {
            for (User user : gettingHelped.get(key)) {
                user.devoice(key);
            }
        }
        gettingHelped.clear();
    }

    @EventType
    public void runEvent(QuitEvent event) {
        String[] chans = event.getUser().getChannels();
        for (String chan : chans) {
            Channel ch = Channel.getChannel(chan);
            Queue<User> queue = queueMap.get(ch);
            if (queue == null) {
                queue = new LinkedBlockingQueue<>();
            }
            queue.remove(event.getUser());
            queueMap.put(ch, queue);
            Set<User> helping = gettingHelped.get(ch);
            if (helping == null) {
                helping = new HashSet<>();
            }
            helping.remove(event.getUser());
            gettingHelped.put(ch, helping);
        }
    }

    @EventType
    public void runEvent(PartEvent event) {
        Channel ch = event.getChannel();
        Queue<User> queue = queueMap.get(ch);
        if (queue == null) {
            queue = new LinkedBlockingQueue<>();
        }
        queue.remove(event.getUser());
        queueMap.put(ch, queue);
        Set<User> helping = gettingHelped.get(ch);
        if (helping == null) {
            helping = new HashSet<>();
        }
        helping.remove(event.getUser());
        gettingHelped.put(ch, helping);
    }

    @EventType
    public void runEvent(KickEvent event) {
        Channel ch = event.getChannel();
        Queue<User> queue = queueMap.get(ch);
        if (queue == null) {
            queue = new LinkedBlockingQueue<>();
        }
        queue.remove(event.getUser());
        queueMap.put(ch, queue);
        Set<User> helping = gettingHelped.get(ch);
        if (helping == null) {
            helping = new HashSet<>();
        }
        helping.remove(event.getUser());
        gettingHelped.put(ch, helping);
    }

    @EventType
    public void runEvent(JoinEvent event) {
        Channel chan = event.getChannel();
        if (!channelsToUseIn.contains(chan.getName())) {
            return;
        }
        User user = event.getUser();
        String userLogin = user.getLoginName();
        for (String name : loginNames) {
            if (userLogin.matches(name)) {
                Queue<User> queue = queueMap.get(event.getChannel());
                queue.offer(user);
                queueMap.put(chan, queue);
                break;
            }
        }
    }

    @Override
    public void runEvent(CommandEvent event) {
        Set<User> gettingAssistance = gettingHelped.get(event.getChannel());
        if (gettingAssistance.contains(event.getUser())) {
            return;
        }
        if (event.getCommand().startsWith("queue")) {
            if (!event.getUser().hasOP(event.getChannel())
                    && !event.getUser().hasPermission(event.getChannel(), "queuesystem.op")) {
                event.getUser().sendNotice("You cannot use that command");
                return;
            }
            if (event.getCommand().endsWith("-off")) {
                activeChans.remove(event.getChannel());
                Set<User> current = gettingHelped.get(event.getChannel());
                for (User user : current) {
                    user.devoice(event.getChannel());
                }
                current.clear();
                gettingHelped.remove(event.getChannel());
                event.getChannel().setMode('m', false);
                event.getUser().sendNotice("I have turned off the queue system");
            } else if (event.getCommand().endsWith("-on")) {
                activeChans.add(event.getChannel());
                event.getChannel().setMode('m', true);
                event.getUser().sendNotice("I have turned on the queue system");
            }
        } else if (event.getCommand().equalsIgnoreCase("next")) {
            if (!event.getUser().hasOP(event.getChannel())
                    && !event.getUser().hasVoice(event.getChannel())
                    && !event.getUser().hasPermission(event.getChannel(), "queuesystem.manage")) {
                event.getUser().sendNotice("You cannot use that command");
                return;
            }
            Queue<User> queue = queueMap.get(event.getChannel());
            User next;
            if (queue == null) {
                queue = new LinkedBlockingQueue<>();
            }
            next = queue.poll();
            queueMap.put(event.getChannel(), queue);

            if (next == null) {
                event.getUser().sendNotice("The queue is empty");
            } else {
                next.voice(event.getChannel());
                event.getChannel().sendMessage(next.getNick() + ", please ask your question now. You will be assisted by " + event.getUser().getNick());
            }
        } else if (event.getCommand().equalsIgnoreCase("done")) {
            if (!event.getUser().hasOP("")
                    && !event.getUser().hasVoice(event.getChannel())
                    && !event.getUser().hasPermission(event.getChannel(), "queuesystem.manage")) {
                event.getUser().sendNotice("You cannot use that command");
                return;
            }
            if (event.getArgs().length == 0) {
                event.getUser().sendNotice("Please specify at least one nick");
            } else {
                Set<User> helping = gettingHelped.get(event.getChannel());
                if (helping == null) {
                    helping = new HashSet<>();
                }
                for (String name : event.getArgs()) {
                    User user = User.getUser(name);
                    if (helping.remove(user)) {
                        user.devoice(event.getChannel());
                    } else {
                        event.getUser().sendNotice(user.getNick() + " was not needing help");
                    }
                }
                gettingHelped.put(event.getChannel(), helping);
            }
        } else if (event.getCommand().equalsIgnoreCase("add")) {
            Channel chan;
            if (event.getArgs().length == 0) {
                if (activeChans.size() == 1) {
                    chan = activeChans.iterator().next();
                } else {
                    event.getUser().sendNotice("I cannot automatically determine the channel to add you to. Please specify the channel by using ``add <channel>");
                    return;
                }
            } else {
                String c = event.getArgs()[0];
                if (c.startsWith("<")) {
                    c = c.substring(1);
                }
                if (c.endsWith(">")) {
                    c = c.substring(0, c.length() - 2);
                }
                chan = Channel.getChannel(c);
            }
            Queue<User> queue = queueMap.get(chan);
            if (queue.contains(event.getUser())) {
                event.getUser().sendNotice("You are already in the queue for " + event.getChannel().getName());
            } else {
                queue.add(event.getUser());
                queueMap.put(chan, queue);
                event.getUser().sendNotice("You have been added to the queue for " + chan.getName() + ". You are number " + queue.size());
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "queue-on",
            "queue-off",
            "next",
            "add",
            "done"
        };
    }
}
