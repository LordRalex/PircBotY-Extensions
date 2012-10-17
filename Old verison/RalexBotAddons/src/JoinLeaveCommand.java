
import com.lordralex.ralexbot.RalexBotMain;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.file.FileSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class JoinLeaveCommand extends Listener {

    private Map<String, String> channelList = new ConcurrentHashMap<String, String>();
    private int MAX_CHANNELS;

    @Override
    public void setup() {
        MAX_CHANNELS = FileSystem.getInt("max-channels");
    }

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
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
            if (channelList.size() >= MAX_CHANNELS && !isMaster(sender)) {
                return;
            }
            channelList.put(channel, sender);
            getPircBot().joinChannel(channel);
        } else if (command.equalsIgnoreCase("leave")) {
            if (args.length == 1) {
                channel = args[0];
            }
            RalexBotMain.print("Leave recieved for channel: " + channel);
            String getJoin = channelList.get(channel);
            if (isMaster(sender) || isOP(sender, channel) || sender.equalsIgnoreCase(getJoin)) {
                channelList.remove(channel);
                getPircBot().partChannel(getPircBot().getChannel(channel), "Told to leave");
            } else {
                sendMessage(sender, "You did not have him join this channel");
            }
        }
    }

    @Override
    public void onNickChange(NickChangeEvent event) {
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

    @Override
    public void declarePriorities() {
        priorities.put(EventType.NickChange, Priority.NORMAL);
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
