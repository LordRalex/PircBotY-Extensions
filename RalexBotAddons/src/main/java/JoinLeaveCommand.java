
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.api.users.BotUser;
import com.lordralex.ralexbot.api.users.User;
import com.lordralex.ralexbot.settings.Settings;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JoinLeaveCommand extends Listener {

    private Map<String, String> channelList = new ConcurrentHashMap<>();
    private int MAX_CHANNELS;
    private Settings settings;

    @Override
    public void setup() {
        settings = new Settings(new File("settings", "config.yml"));
        MAX_CHANNELS = settings.getInt("max-channels");
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        String command = event.getCommand().toLowerCase().trim();
        String[] args = event.getArgs();
        Channel channel = event.getChannel();
        User sender = event.getSender();
        if (command.equalsIgnoreCase("join")) {
            if (args.length != 1) {
                return;
            }
            channel = Channel.getChannel(args[0]);
            if (channelList.containsKey(channel.getName())) {
                return;
            }
            if (channelList.size() >= MAX_CHANNELS) {
                return;
            }
            channelList.put(channel.getName(), sender.getNick());
            BotUser.getBotUser().joinChannel(channel.getName());
        } else if (command.equalsIgnoreCase("leave")) {
            if (args.length == 1) {
                channel = Channel.getChannel(args[0]);
            }
            String getJoin = channelList.get(channel.getName());
            if (sender.hasOP(channel.getName()) || sender.getNick().equalsIgnoreCase(getJoin)) {
                channelList.remove(channel.getName());
                BotUser.getBotUser().leaveChannel(channel.getName());
            } else {
                sender.sendMessage("You did not have him join this channel");
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
