
import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.events.ActionEvent;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.api.users.BotUser;
import com.lordralex.ralexbot.settings.Settings;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Joshua
 */
public class CensorListener extends Listener {

    private final List<String> censor = new ArrayList<>();
    private final List<String> channels = new ArrayList<>();
    private final Set<String> warned = new HashSet<>();

    @Override
    public void setup() {
        censor.clear();
        censor.addAll(Settings.getGlobalSettings().getStringList("censor"));
        channels.clear();
        channels.addAll(Settings.getGlobalSettings().getStringList("censor-channels"));
    }

    @Override
    @EventType(event = EventField.Message)
    public void runEvent(MessageEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        if (event.getSender().getNick().equalsIgnoreCase(BotUser.getBotUser().getNick())) {
            return;
        }
        String message = event.getMessage().toLowerCase();
        if (scanMessage(message)) {
            if (!RalexBot.getDebugMode()) {
                if (warned.contains(event.getSender().getNick()) || warned.contains(event.getSender().getIP())) {
                    BotUser.getBotUser().kick(event.getSender().getNick(), event.getChannel().getName(), "Please keep it civil");
                } else {
                    warned.add(event.getSender().getNick());
                    warned.add(event.getSender().getIP());
                    event.getChannel().sendMessage("Please keep it civil, " + event.getSender().getNick());
                }
            }
            if (RalexBot.getDebugMode()) {
                BotUser.getBotUser().sendMessage(Settings.getGlobalSettings().getString("debug-channel"), event.getSender().getNick() + " triggered the censor with his line: " + event.getMessage());
            }
        }
    }

    @Override
    @EventType(event = EventField.Notice)
    public void runEvent(ActionEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        if (event.getSender().getNick().equalsIgnoreCase(BotUser.getBotUser().getNick())) {
            return;
        }
        String message = event.getAction().toLowerCase();
        if (scanMessage(message)) {
            if (!RalexBot.getDebugMode()) {
                if (warned.contains(event.getSender().getNick()) || warned.contains(event.getSender().getIP())) {
                    BotUser.getBotUser().kick(event.getSender().getNick(), event.getChannel().getName(), "Please keep it civil");
                } else {
                    warned.add(event.getSender().getNick());
                    warned.add(event.getSender().getIP());
                    event.getChannel().sendMessage("Please keep it civil, " + event.getSender().getNick());
                }
            }
            if (RalexBot.getDebugMode()) {
                BotUser.getBotUser().sendMessage(Settings.getGlobalSettings().getString("debug-channel"), event.getSender().getNick() + " triggered the censor with his line: " + event.getAction());
            }
        }
    }

    @Override
    @EventType(event = EventField.NickChange)
    public void runEvent(NickChangeEvent event) {
        String message = event.getNewNick().toLowerCase();
        if (scanMessage(message)) {
            for (String chan : BotUser.getBotUser().getChannels()) {
                if (!RalexBot.getDebugMode()) {
                    BotUser.getBotUser().kick(event.getNewNick(), chan, "Please keep it civil");
                }
            }
        }
    }

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        String message = event.getSender().getNick().toLowerCase();
        if (scanMessage(message)) {
            if (!RalexBot.getDebugMode()) {
                BotUser.getBotUser().kick(event.getSender().getNick(), event.getChannel().getName(), "Please keep it civil");
            }
        }
    }

    private boolean scanMessage(String message) {
        for (String word : censor) {
            String[] parts = message.split(" ");
            for (String p : parts) {
                if (p.equalsIgnoreCase(word)) {
                    return true;
                }
            }
        }
        return false;
    }
}
