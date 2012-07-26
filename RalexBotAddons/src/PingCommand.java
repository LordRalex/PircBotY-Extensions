

import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class PingCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String channel = event.getChannel();
        String sender = event.getSender();
        String target = null;
        if (channel != null) {
            target = channel;
        } else if (sender != null) {
            target = sender;
        }
        if (target == null) {
            return;
        }
        sendMessage(target, "Yes, i can hear you");
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "ping"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
