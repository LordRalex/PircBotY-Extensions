
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import java.util.Random;
import org.jibble.pircbot.Colors;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Joshua
 */
public class EightBallCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        String dest = event.getChannel();
        if (dest == null) {
            dest = event.getSender();
            if (dest == null) {
                return;
            }
        }

        int random = new Random().nextInt(10);
        String reply = "";

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

        sendMessage(dest, event.getSender() + ": " + reply);
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
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
