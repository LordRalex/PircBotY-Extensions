
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.users.User;

public class GuestListener extends Listener {

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        if (event.isCancelled()) {
            return;
        }
        User sender = event.getSender();
        String nick = sender.getNick().toLowerCase().trim();
        if (nick.startsWith("guest") || nick.startsWith("mib_")) {
            sender.sendNotice("Please use /nick <name> to change your name");
            sender.sendNotice("I am only a bot though, so i will not reply to your pms");
        }
    }
}
