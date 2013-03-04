
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.sender.Sender;

/**
 *
 * @author Joshua
 */
public class UCommand extends Listener {

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
        if (event.getArgs().length == 0) {
            target.sendMessage(event.getSender().getNick() + ", " + "$u <user> [profile, posts, topics, warnings, videos, friends, pm, names, admin, edit, modcp, validate, warn, suspend, iphistory]");
        } else {
            String link = "http://u.mcf.li/" + event.getArgs()[0];
            if (event.getArgs().length >= 2) {
                link += "/" + event.getArgs()[1];
            }
            target.sendMessage(event.getSender().getNick() + ", " + link);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "u"
        };
    }
}
