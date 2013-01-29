
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utilities;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.JoinEvent;
import java.util.HashMap;
import java.util.Map;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Joshua
 */
public class SayBackCommand extends Listener {

    private boolean say = false;
    private Map<String, String> mappings = new HashMap<>();

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        if (say) {
            String message = mappings.get(event.getChannel().getName().toLowerCase());
            if (message == null || message.isEmpty()) {
                return;
            }
            event.getChannel().sendMessage(event.getSender().getNick() + ", " + message);
        }
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.getSender().hasVoice(event.getChannel().getName())
                || event.getSender().hasOP(event.getChannel().getName())) {
            if (event.getArgs().length == 0) {
                say = false;
                mappings.remove(event.getChannel().getName().toLowerCase());
                event.getChannel().sendMessage("I will stop telling people when they join");
            } else {
                mappings.put(event.getChannel().getName().toLowerCase(), Utilities.toString(event.getArgs()));
                event.getChannel().sendMessage("I will start telling people when they join");
                say = true;
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "message"
                };
    }
}
