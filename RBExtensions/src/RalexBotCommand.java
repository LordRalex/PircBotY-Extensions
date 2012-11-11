
import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import org.pircbotx.PircBotX;

public class RalexBotCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
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
        Utils.sendMessage(target, "Hello. I am " + Utils.getNick() + " " + RalexBot.VERSION + " using PircBotX " + PircBotX.VERSION);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    Utils.getNick().toLowerCase(),
                    "version"
                };
    }
}
