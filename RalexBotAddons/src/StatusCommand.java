import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class StatusCommand extends Listener {

    Map<String, Boolean> memStatus = new HashMap<String, Boolean>();

    @Override
    public void onPart(PartEvent event) {
        String sender = event.getSender();
        if (memStatus.containsKey(sender)) {
            memStatus.remove(sender);
        }
    }

    @Override
    public void onNickChange(NickChangeEvent event) {
        String oldNick = event.getOldNick();
        String newNick = event.getNewNick();
        if (memStatus.containsKey(oldNick)) {
            Boolean status = memStatus.remove(oldNick);
            memStatus.put(newNick, status);
        }
    }

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final String sender = event.getSender();
        final String[] args = event.getArgs();

        if (args.length == 0) {
            return;
        }
        boolean newStatus = Boolean.parseBoolean(args[0]);
        if (!newStatus) {
            memStatus.remove(sender);
            sendMessage(sender, "You are now here");
            return;
        }
        if (memStatus.containsKey(sender)) {
            sendMessage(sender, "You are already away");
            return;
        }
        memStatus.put(sender, Boolean.TRUE);
        sendMessage(sender, "You are now away");
    }

    private void checkForAway(String sender, String[] messageParts) {
        Object[] nameList = (Object[]) memStatus.keySet().toArray();

        for (String possible : messageParts) {
            for (Object name : nameList) {
                if (possible.toLowerCase().contains(((String) name).toLowerCase())) {
                    sendMessage(sender, name + " is away");
                }
            }
        }
    }

    @Override
    public void onMessage(MessageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String message = event.getMessage();
        String sender = event.getSender();
        if (!message.startsWith("*")) {
            String[] possibleNames = message.split(" ");
            checkForAway(sender, possibleNames);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"status"};
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Message, Priority.NORMAL);
        priorities.put(EventType.Part, Priority.NORMAL);
        priorities.put(EventType.Command, Priority.NORMAL);
        priorities.put(EventType.NickChange, Priority.NORMAL);

    }
}
