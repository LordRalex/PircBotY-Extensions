import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import java.util.ArrayList;
import java.util.List;
import org.jibble.pircbot.User;

/**
 * @version 1.0
 * @author Joshua
 */
public class SilentCommand extends Listener {

    List<String> silenced = new ArrayList<>();

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        switch (event.getCommand()) {
            case "silent":
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
                break;
            case "unsilent":
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
                break;
        }
        if (event.getChannel() != null && silenced.contains(event.getChannel().toLowerCase())) {
            String sender = event.getSender();
            boolean canUse = false;
            User[] users = getBot().getUsers(event.getChannel());
            for (User user : users) {
                if (user.getNick().equalsIgnoreCase(sender)) {
                    if (user.hasVoice() || user.isOp()) {
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
