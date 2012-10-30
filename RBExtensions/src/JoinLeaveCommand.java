
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.settings.Settings;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JoinLeaveCommand extends Listener {

    private Map<String, String> channelList = new ConcurrentHashMap<String, String>();
    private int MAX_CHANNELS;

    @Override
    public void setup() {
        MAX_CHANNELS = Settings.getInt("max-channels");
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        String command = event.getCommand().toLowerCase().trim();
        String[] args = event.getArgs();
        String channel = event.getChannel();
        String sender = event.getSender();
        if (command.equalsIgnoreCase("join")) {
            if (args.length != 1) {
                return;
            }
            channel = args[0];
            if (channelList.containsKey(channel)) {
                return;
            }
            if (channelList.size() >= MAX_CHANNELS) {
                return;
            }
            channelList.put(channel, sender);
            Utils.joinChannel(channel);
        } else if (command.equalsIgnoreCase("leave")) {
            if (args.length == 1) {
                channel = args[0];
            }
            String getJoin = channelList.get(channel);
            if (Utils.hasOP(sender, channel) || sender.equalsIgnoreCase(getJoin)
                    || Utils.isIdentifedAs(sender, "Lord_Ralex")) {
                channelList.remove(channel);
                Utils.leaveChannel(channel);
            } else {
                Utils.sendMessage(sender, "You did not have him join this channel");
            }
        }
    }

    @Override
    @EventType(event = EventField.NickChange)
    public void runEvent(NickChangeEvent event) {
        String oldNick = event.getOldNick();
        String newNick = event.getNewNick();
        if (channelList.containsValue(oldNick)) {
            String[] keys = channelList.keySet().toArray(new String[0]);
            for (String channel : keys) {
                String old = channelList.get(channel);
                if (old != null && old.equalsIgnoreCase(oldNick)) {
                    channelList.remove(channel);
                    channelList.put(channel, newNick);
                }
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "join",
                    "leave"
                };
    }
}
