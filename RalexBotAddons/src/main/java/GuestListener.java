
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.JoinEvent;


public class GuestListener extends Listener {

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String sender = event.getSender();
        sender = sender.toLowerCase().trim();
        if (sender.startsWith("guest") || sender.startsWith("mib_")) {
            Utils.sendNotice(sender, "Please use /nick <name> to change your name");
            Utils.sendNotice(sender, "I am only a bot though, so i will not reply to your pms");
        }
    }
}
