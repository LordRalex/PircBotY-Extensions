import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class PlayerSlotCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        String channel = event.getChannel();
        String sender = event.getSender();
        String[] args = event.getArgs();

        if (channel == null) {
            channel = sender;
        }
        if (sender == null) {
            return;
        }
        if (args.length != 3) {
            sendMessage(channel, "The correct usage of this command is: *slots <Ram in GB> <Download speed in MB> <Upload speed in MB>");
            return;
        }
        int ram = Integer.parseInt(args[0]) * 1024;
        int dl = (int) Double.parseDouble(args[1]) * 1024;
        int ul = (int) (Double.parseDouble(args[2]) * 1024);

        int maxslots = (ram - 240) / 90;
        int temp = ul / 120;
        if (temp < maxslots) {
            maxslots = temp;
        }
        sendMessage(channel, "I estimate your server to be able to handle " + maxslots + " players at once");
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "slots"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
