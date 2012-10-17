
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.pircbotx.User;

/**
 * @version 1.0
 * @author Joshua
 */
public class SilentCommand extends Listener {

    List<String> silenced = new ArrayList<String>();

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!isOP(event.getSender(), event.getChannel())) {
            return;
        }
        if (event.getCommand().equalsIgnoreCase("silent")) {
            if (event.getArgs().length == 0 && event.getChannel() != null) {
                String channel = event.getChannel().toLowerCase();
                silenced.remove(channel);
                silenced.add(channel);
                sendMessage(channel, "I am now going into quiet mode");
            } else {
                String channel = event.getArgs()[0].toLowerCase();
                if (!channel.startsWith("#")) {
                    channel = "#" + channel;
                }
                silenced.remove(channel);
                silenced.add(channel);
                sendMessage(event.getSender(), "I am now going into quiet mode for channel: " + channel);
            }
        }


        if (event.getCommand().equalsIgnoreCase("unsilent")) {
            if (event.getArgs().length == 0 && event.getChannel() != null) {
                String channel = event.getChannel().toLowerCase();
                silenced.remove(channel);
                sendMessage(channel, "I am now going into loud mode");
            } else {
                String channel = event.getArgs()[0].toLowerCase();
                if (!channel.startsWith("#")) {
                    channel = "#" + channel;
                }
                silenced.remove(channel);
                silenced.add(channel);
                sendMessage(event.getSender(), "I am now going into loud mode for channel: " + channel);
            }
        }


        if (event.getChannel() != null && silenced.contains(event.getChannel().toLowerCase())) {
            String sender = event.getSender();
            boolean canUse = false;
            Set<User> users = getPircBot().getUsers(getPircBot().getChannel(event.getChannel()));
            for (User user : users) {
                if (user.getNick().equalsIgnoreCase(sender)) {
                    if (user.getChannelsVoiceIn().contains(getPircBot().getChannel(event.getChannel())) || user.getChannelsOpIn().contains(getPircBot().getChannel(event.getChannel()))) {
                        canUse = true;
                        break;
                    }
                }
            }
            if (!canUse) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.LOWEST);
    }

    @Override
    public String[] getAliases() {
        return null;
    }
}
