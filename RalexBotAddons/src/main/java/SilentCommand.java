
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import java.util.ArrayList;
import java.util.List;

public class SilentCommand extends Listener {

    List<String> silenced = new ArrayList<>();

    @Override
    @EventType(event = EventField.Command, priority = Priority.HIGH)
    public void runEvent(CommandEvent event) {
        if (!event.getSender().hasOP(event.getChannel().getName())) {
            return;
        }
        if (event.getCommand().equalsIgnoreCase("silent")) {
            if (event.getArgs().length == 0 && event.getChannel() != null) {
                String channel = event.getChannel().getName().toLowerCase();
                silenced.remove(channel);
                silenced.add(channel);
                event.getChannel().sendMessage("I am now going into quiet mode");
            } else {
                String channel = event.getArgs()[0].toLowerCase();
                if (!channel.startsWith("#")) {
                    channel = "#" + channel;
                }
                silenced.remove(channel);
                silenced.add(channel);
                event.getSender().sendMessage("I am now going into quiet mode for channel: " + channel);
            }
        }


        if (event.getCommand().equalsIgnoreCase("unsilent")) {
            if (event.getArgs().length == 0 && event.getChannel() != null) {
                String channel = event.getChannel().getName().toLowerCase();
                silenced.remove(channel);
                event.getChannel().sendMessage("I am now going into loud mode");
            } else {
                String channel = event.getArgs()[0].toLowerCase();
                if (!channel.startsWith("#")) {
                    channel = "#" + channel;
                }
                silenced.remove(channel);
                silenced.add(channel);
                event.getSender().sendMessage("I am now going into loud mode for channel: " + channel);
            }
        }


        if (event.getChannel() != null && silenced.contains(event.getChannel().getName().toLowerCase())) {
            boolean canUse = false;
            if (event.getSender().hasVoice(event.getChannel().getName()) || event.getSender().hasOP(event.getChannel().getName())) {
                canUse = true;
            }
            if (!canUse) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "silent",
                    "unsilent"
                };
    }
}
