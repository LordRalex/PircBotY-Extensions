
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
import com.lordralex.ralexbot.api.events.ActionEvent;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.users.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class NotifyListener extends Listener {

    private final List<String[]> list = new ArrayList<>();

    @Override
    @EventType(event = EventField.Action)
    public void runEvent(ActionEvent event) {
        runCheck(event.getSender().getNick(), event.getChannel().getName(), "used an action");
    }

    @Override
    @EventType(event = EventField.Message)
    public void runEvent(MessageEvent event) {
        runCheck(event.getSender().getNick(), event.getChannel().getName(), "talked");
    }

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        runCheck(event.getSender().getNick(), event.getChannel().getName(), "joined");
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.getArgs().length != 1) {
            event.getChannel().sendMessage("Usage: notify [name]");
            return;
        }
        String[] array = new String[2];
        array[0] = event.getArgs()[0];
        array[1] = event.getSender().getNick();
        event.getChannel().sendMessage(event.getSender().getNick() + ": You will be told when I see " + event.getArgs()[0]);
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
