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
import com.lordralex.ralexbot.api.Utilities;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.sender.Sender;
import com.lordralex.ralexbot.api.users.BotUser;
import java.util.Random;

public class ChooseCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        Sender target = event.getChannel();
        if (target == null) {
            target = event.getSender();
        }
        if (target == null) {
            return;
        }

        String[] args = event.getArgs();
        if (args.length == 0) {
            target.sendMessage("Command use: *choose [choices] (you can use spaces or , to separate them)");
            return;
        }

        String total = Utilities.toString(args);
        String[] choices;
        if (total.contains(",")) {
            choices = total.split(",");
        } else {
            choices = total.split(" ");
        }
        if (choices.length <= 1) {
            target.sendMessage("What kind of a choice is that?");
            return;
        }

        String answer = choices[new Random().nextInt(choices.length)];
        answer = answer.trim();
        if (event.getSender() != null) {
            target.sendMessage(event.getSender().getNick() + ": " + answer);
        } else {
            target.sendMessage(answer);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "choose"
        };
    }
}
