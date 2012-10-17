
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.events.PartEvent;
import com.lordralex.ralexbot.api.events.QuitEvent;
import java.util.ArrayList;
import java.util.List;

public class ServerIPListener extends Listener {

    PingServerCommand pingServer = new PingServerCommand();
    List<String> triggered = new ArrayList<String>();

    @Override
    @EventType(event = EventField.Message, priority = Priority.HIGH)
    public void runEvent(MessageEvent event) {
        String channel = event.getChannel();
        String sender = event.getSender();
        String message = event.getMessage();

        boolean silence = false;

        if (triggered.contains(sender.toLowerCase())) {
            silence = true;
        }
        String[] messageParts = message.split(" ");
        for (String part : messageParts) {
            if (isServer(part)) {
                if (!silence) {
                    Utils.sendMessage(channel, "Please do not advertise servers here");
                    triggered.remove(sender.toLowerCase());
                    triggered.add(sender.toLowerCase());
                } else if (triggered.contains(sender.toLowerCase())) {
                    Utils.kick(sender, channel, "Server advertisement");
                }
                break;
            }
        }
    }

    @Override
    @EventType(event = EventField.Part)
    public void runEvent(PartEvent event) {
        if (event.isCancelled()) {
            return;
        }
        triggered.remove(event.getSender().toLowerCase());
    }

    @Override
    @EventType(event = EventField.Quit)
    public void runEvent(QuitEvent event) {
        if (event.isCancelled()) {
            return;
        }
        triggered.remove(event.getSender().toLowerCase());
    }

    private boolean isServer(String testString) {
        String test = testString.toLowerCase().trim();

        if (test.split(".").length == 4) {
            String[] parts = test.split(".");
            if (parts[3].contains(":")) {
                parts[3] = parts[3].split(":")[0];
            }
            for (int i = 0; i < 4; i++) {
                try {
                    Integer.parseInt(parts[i]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }
}
