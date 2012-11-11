
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.settings.Settings;
import java.util.List;

public class HelpCommand extends Listener {

    public String[] help;

    @Override
    public void setup() {
        List<String> helpLines = Settings.getStringList("help-list");
        help = helpLines.toArray(new String[0]);
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String sender = event.getSender();
        String channel = event.getChannel();
        String helpLine = "My commands you can know about: ";
        for (String name : help) {
            helpLine += name + ", ";
        }
        helpLine = helpLine.trim();
        if (channel == null) {
            channel = sender;
        }
        if (channel == null) {
            return;
        }
        Utils.sendMessage(channel, helpLine);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "help",
                    "commands"
                };
    }
}
