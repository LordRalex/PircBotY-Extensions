import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.0
 * @author Joshua
 */
public class ExpandCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final String channel = event.getChannel();
        final String sender = event.getSender();
        final String[] args = event.getArgs();
        try {
            String target = channel;
            if (target == null) {
                target = sender;
            }
            if (target == null) {
                return;
            }
            if (args.length == 0) {
                sendMessage(target, "*expand <link>");
                return;
            }
            String finalLink = parse(args[0]);
            sendMessage(target, finalLink);
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(MCFCommand.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "expand"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
