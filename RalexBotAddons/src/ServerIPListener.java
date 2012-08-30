
import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.*;
import java.util.ArrayList;
import java.util.List;
import org.pircbotx.PircBotX;

/**
 * @version 1.0
 * @author Joshua
 */
public class ServerIPListener extends Listener {

    PingServerCommand pingServer = new PingServerCommand();
    boolean silence = false;
    List<String> triggered = new ArrayList<>();

    @Override
    public void onMessage(MessageEvent event) {
        if (event.isCancelled()) {
            return;
        }

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
                    sendMessage(channel, "Please do not advertise servers here");
                    triggered.remove(sender.toLowerCase());
                    triggered.add(sender.toLowerCase());
                } else if (triggered.contains(sender.toLowerCase())) {
                    PircBotX bot = getPircBot();
                    if (isOP(bot.getNick(), channel)) {
                        bot.kick(bot.getChannel(channel), bot.getUser(sender), "Advertising a server");
                    } else {
                        bot.sendMessage("chanserv", "kick " + channel + " " + sender + " " + "Advertising a server");
                    }
                }
                silence = true;
            }
        }
        silence = false;
    }

    @Override
    public void onPart(PartEvent event) {
        if (event.isCancelled()) {
            return;
        }
        triggered.remove(event.getSender().toLowerCase());
    }

    @Override
    public void onQuit(QuitEvent event) {
        if (event.isCancelled()) {
            return;
        }
        triggered.remove(event.getQuitter().toLowerCase());
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

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Message, Priority.NORMAL);
        priorities.put(EventType.Part, Priority.NORMAL);
        priorities.put(EventType.Quit, Priority.NORMAL);
    }
}
