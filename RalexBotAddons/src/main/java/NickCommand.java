
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;

public class NickCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String sender = event.getSender(), channel = event.getChannel();
        String[] args = event.getArgs();
        if (!Utils.hasOP(sender, channel)) {
            return;
        }
        String newNick;
        if (args.length == 1) {
            newNick = args[0];
        } else {
            newNick = "RalexBot";
        }
        Utils.setNick(newNick);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "nick"
                };
    }
}
