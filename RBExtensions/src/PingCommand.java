
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;

public class PingCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
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

        Utils.sendMessage(target, "Yes, i can hear you");
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "ping"
                };
    }
}
