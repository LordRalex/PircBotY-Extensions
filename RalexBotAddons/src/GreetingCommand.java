import com.lordralex.ralexbot.RalexBotMain;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import com.lordralex.ralexbot.api.events.JoinEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class GreetingCommand extends Listener {

    Map<String, String> greetings = new HashMap<>();

    //TODO: Fix command
    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String sender = event.getSender();
        String channel = event.getChannel();
        String[] args = event.getArgs();
        if (!isMaster(sender)) {
            if (channel != null && sender != null) {
                if (!isVoice(sender, channel) && !isOP(sender, channel)) {
                    return;
                }
            }
        }
        String completemessage = "";

        for (int i = 0; i < args.length; i++) {
            completemessage += args[i] + " ";
        }
        if (channel == null) {
            channel = args[0];
            completemessage = "";
            for (int i = 1; i < args.length; i++) {
                completemessage += args[i] + " ";
            }
        }
        if (channel == null) {
            return;
        }

        completemessage = completemessage.trim();
        if (completemessage.equalsIgnoreCase("null") || completemessage.equalsIgnoreCase("")) {
            greetings.remove(channel);
            if (sender == null) {
                RalexBotMain.print("Greeting for " + channel + " removed");
            } else {
                sendMessage(sender, "Greeting for " + channel + " was removed");
            }
            return;
        }
        greetings.put(channel, completemessage);
        if (sender == null) {
            RalexBotMain.print("Greeting for " + channel + " was changed to:");
            RalexBotMain.print(greetings.get(channel));
        } else {
            sendMessage(sender, "Greeting for " + channel + " was changed to:");
            sendMessage(sender, greetings.get(channel));
        }
    }

    @Override
    public void onJoin(JoinEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String channel = event.getChannel();
        String sender = event.getSender();
        String welcome;
        if ((welcome = greetings.get(channel)) == null) {
            return;
        }
        if (welcome.equalsIgnoreCase("") || welcome.equalsIgnoreCase("null")) {
            return;
        }
        welcome = replacePlaceHolders(sender, channel, welcome, new String[0]);
        sendNotice(sender, welcome);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "setwelcome",};
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Join, Priority.NORMAL);
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
