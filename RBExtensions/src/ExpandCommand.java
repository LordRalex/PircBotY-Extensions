
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExpandCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final String channel = event.getChannel();
        final String sender = event.getSender();
        final String[] args = event.getArgs();
        String target = channel;
        if (target == null) {
            target = sender;
        }
        if (target == null) {
            return;
        }
        try {
            if (args.length == 0) {
                Utils.sendMessage(target, "*expand <link>");
                return;
            }
            String finalLink = Utils.parse(args[0]);
            Utils.sendMessage(target, finalLink);
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(MCFCommand.class.getName()).log(Level.SEVERE, null, ex);
            Utils.sendMessage(target, "There was a problem expanding that");
        }

    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "expand"
                };
    }
}
