
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;

/**
 * @version 1.1
 * @author Lord_Ralex
 * @since 1.0
 */
public class HelpCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String sender = event.getSender();
        String channel = event.getChannel();
        String[] help = new String[]{
            "My commands you can know about: ping, deadfly, ei, expand, google, "
            + "gis, help, login, mcf, mcfprofile, pingserver, slots, plugin, "
            + getPircBot().getNick().toLowerCase() + ", rem, siteping, "
            + "status, tell, yaml, youtube"
        };
        if (channel == null) {
            channel = sender;
        }
        if (channel == null) {
            return;
        }
        sendMessage(channel, help);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "help",
                    "commands"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
