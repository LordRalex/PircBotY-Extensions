
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.events.PartEvent;
import com.lordralex.ralexbot.api.events.QuitEvent;
import java.util.ArrayList;
import java.util.List;

public class ServerIPListener extends Listener {

    PingServerCommand pingServer = new PingServerCommand();
    boolean silence = false;
    List<String> triggered = new ArrayList<>();

    @Override
    @EventType(event = EventField.Message)
    public void runEvent(MessageEvent event) {
        final String channel = event.getChannel();
        final String sender = event.getSender();
        final String message = event.getMessage();

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
                silence = true;
            }
        }
        silence = false;
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(PartEvent event) {
        if (event.isCancelled()) {
            return;
        }
        triggered.remove(event.getSender().toLowerCase());
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(QuitEvent event) {
        if (event.isCancelled()) {
            return;
        }
        triggered.remove(event.getSender().toLowerCase());
    }

    private boolean isServer(String testString) {
        String test = testString.toLowerCase().trim();
        List<String> tempList = new ArrayList<>();
        int lastOccurance = 0;
        for (int i = 0; i < test.length(); i++) {
            if (test.charAt(i) == '.') {
                tempList.add(test.substring(lastOccurance, i));
                lastOccurance = i;
            }
        }
        String[] parts = tempList.toArray(new String[0]);
        if (parts.length == 2 || parts.length == 3 || parts.length == 4) {
            String ip = test.split(":")[0];
            String port = "25565";
            if (test.split(":").length == 2) {
                port = test.split(":")[1];
            }
            int p;
            try {
                p = Integer.parseInt(port);
                Object[] result = pingServer.test(ip, p);
                return (result[0] == Boolean.TRUE);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
}
