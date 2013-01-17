
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utilities;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.sender.Sender;
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

        final String[] args = event.getArgs();
        Sender target = event.getChannel();
        if (target == null) {
            target = event.getSender();
        }
        if (target == null) {
            return;
        }
        try {
            if (args.length == 0) {
                target.sendMessage("*expand <link>");
                return;
            }
            String finalLink = Utilities.resolve(args[0]);
            target.sendMessage(event.getSender().getNick() + ": " + finalLink);
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(MCFCommand.class.getName()).log(Level.SEVERE, null, ex);
            target.sendMessage("There was a problem expanding that");
        }

    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "expand"
                };
    }
}
