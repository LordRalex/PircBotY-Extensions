

import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;

/**
 * @version 1.1
 * @author Lord_Ralex
 * @since 1.0
 */
public class SayCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final String channel = event.getChannel();
        final String sender = event.getSender();
        final String[] args = event.getArgs();

        String message = "";
        for (String part : args) {
            message += part + " ";
        }
        String target = channel;
        if (channel == null) {
            target = sender;
        }
        if (sender == null) {
            target = message.split(" ")[0];
            message = message.split(" ", 2)[1];
        }
        if (target == null) {
            target = getBot().getNick();
        }
        if (target != null) {
            sendMessage(target, message);
            System.out.println("Message sent");
        }
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
