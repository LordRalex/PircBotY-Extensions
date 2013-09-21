
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
import net.ae97.aebot.api.EventType;
import net.ae97.aebot.api.Listener;
import net.ae97.aebot.api.events.ActionEvent;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.events.JoinEvent;
import net.ae97.aebot.api.events.MessageEvent;
import net.ae97.aebot.api.users.User;
import java.util.ArrayList;
import java.util.List;
import net.ae97.aebot.api.CommandExecutor;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class NotifyListener extends CommandExecutor implements Listener {

    private final List<String[]> list = new ArrayList<>();

    @EventType
    public void runEvent(ActionEvent event) {
        runCheck(event.getUser().getNick(), event.getChannel().getName(), "used an action");
    }

    @EventType
    public void runEvent(MessageEvent event) {
        runCheck(event.getUser().getNick(), event.getChannel().getName(), "talked");
    }

    @EventType
    public void runEvent(JoinEvent event) {
        runCheck(event.getUser().getNick(), event.getChannel().getName(), "joined");
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (event.getArgs().length != 1) {
            event.getChannel().sendMessage("Usage: notify [name]");
            return;
        }
        String[] array = new String[2];
        array[0] = event.getArgs()[0];
        array[1] = event.getUser().getNick();
        event.getChannel().sendMessage(event.getUser().getNick() + ": You will be told when I see " + event.getArgs()[0]);
        list.add(array);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "notify"
        };
    }

    private void runCheck(String nowActive, String chan, String action) {
        List<String[]> toRemove = new ArrayList<>();
        for (String[] set : list) {
            if (set[0].equalsIgnoreCase(nowActive)) {
                User user = User.getUser(set[1]);
                user.sendNotice(nowActive + " has just " + action + " in #" + chan);
                toRemove.add(set);
            }
        }
        list.removeAll(toRemove);
    }
}
