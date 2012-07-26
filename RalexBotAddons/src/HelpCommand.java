

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
            "My commands you can know about: ping, pingserver <ip>, version, ralexbot, tell <name> <message>, yaml <pastebin link>, login, plugin <plugin name or description>, siteping <link>, mcf <search>, mcfprofile <name>"
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
