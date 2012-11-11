
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;

public class HelpCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String sender = event.getSender();
        String channel = event.getChannel();
        String[] help = new String[]{
            "My commands you can know about: ping, deadfly, ei, expand, google, "
            + "gis, help, login, mcf, mcfprofile, pingserver, slots, plugin, "
            + Utils.getNick().toLowerCase() + ", rem, siteping, "
            + "status, tell, yaml, youtube"
        };
        if (channel == null) {
            channel = sender;
        }
        if (channel == null) {
            return;
        }
        Utils.sendMessage(channel, help);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "help",
                    "commands"
                };
    }
}
