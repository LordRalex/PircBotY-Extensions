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
import com.lordralex.ralexbot.api.sender.Sender;
import java.util.Random;
import org.pircbotx.Colors;

public class EightBallCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        Sender dest = event.getChannel();
        if (dest == null) {
            dest = event.getSender();
            if (dest == null) {
                return;
            }
        }

        int random = new Random().nextInt(10);
        String reply;

        switch (random) {
            case 0:
                reply = Colors.RED + "Not at all";
                break;
            case 1:
                reply = Colors.GREEN + "Sure, it seems like it";
                break;
            case 2:
                reply = Colors.RED + "It is not likely";
                break;
            case 3:
                reply = Colors.RED + "I will say no";
                break;
            case 4:
                reply = Colors.RED + "Does not seem like it";
                break;
            case 5:
                reply = Colors.BROWN + "I cannot tell you that now";
                break;
            case 6:
                reply = Colors.BROWN + "Reply is hazy";
                break;
            case 7:
                reply = Colors.GREEN + "Sure";
                break;
            case 8:
                reply = Colors.GREEN + "It is certain";
                break;
            case 9:
                reply = Colors.GREEN + "Your fortune seems good";
                break;
            default:
                reply = Colors.RED + "No";
                break;
        }

        dest.sendMessage(event.getSender().getNick() + ": " + reply);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "ei",
            "8ball",
            "eightball"
        };
    }
}
