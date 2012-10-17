import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.EventType;
import com.lordralex.ralexbot.api.events.JoinEvent;

/**
 * @version 1.0
 * @author Joshua
 */
public class GuestListener extends Listener {

    @Override
    public void onJoin(JoinEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String sender = event.getSender();
        sender = sender.toLowerCase().trim();
        if (sender.startsWith("guest") || sender.startsWith("mib_")) {
            sendNotice(sender, "Please use /nick <name> to change your name");
            sendNotice(sender, "I am only a bot though, so i will not reply to your pms");
        }
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Join, Priority.NORMAL);
    }
}
