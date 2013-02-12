
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.api.events.PartEvent;
import com.lordralex.ralexbot.api.events.QuitEvent;
import com.lordralex.ralexbot.api.users.User;
import com.lordralex.ralexbot.settings.Settings;
import java.util.ArrayList;
import java.util.List;

public class HelloBackListener extends Listener {

    private List<HBLUser> logins = new ArrayList<>();
    private List<String> hellos = new ArrayList<>();
    private Settings settings;
    private final List<String> channels = new ArrayList<>();

    @Override
    public void setup() {
        settings = Settings.getGlobalSettings();
        List<String> more = settings.getStringList("greetings");
        if (more != null && !more.isEmpty()) {
            for (String string : more) {
                hellos.add(string.toLowerCase());
            }
        }
        channels.clear();
        channels.addAll(Settings.getGlobalSettings().getStringList("hello-channels"));
    }

    @Override
    @EventType(event = EventField.Message)
    public void runEvent(MessageEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        String message = event.getMessage();
        Channel channel = event.getChannel();
        User sender = event.getSender();
        if (isGreeting(message)) {
            for (int i = 0; i < logins.size(); i++) {
                if (logins.get(i).equals(sender.getNick(), channel.getName())) {
                    HBLUser user = logins.remove(i);
                    i--;
                    if (user.isTime()) {
                        channel.sendMessage("Hello " + sender.getNick());
                    }
                }
            }
        }
    }

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        String sender = event.getSender().getNick();
        String channel = event.getChannel().getName();
        logins.add(new HBLUser(sender, channel, System.currentTimeMillis()));
    }

    @Override
    @EventType(event = EventField.Part)
    public void runEvent(PartEvent event) {
        String sender = event.getSender().getNick();
        String channel = event.getChannel().getName();
        for (int i = 0; i < logins.size(); i++) {
            if (logins.get(i).equals(sender, channel)) {
                logins.remove(i);
                i--;
            }
        }
    }

    @Override
    @EventType(event = EventField.Quit)
    public void runEvent(QuitEvent event) {
        String sourceNick = event.getSender().getNick();
        for (int i = 0; i < logins.size(); i++) {
            if (logins.get(i).equals(sourceNick)) {
                logins.remove(i);
                i--;
            }
        }
    }

    @Override
    @EventType(event = EventField.NickChange)
    public void runEvent(NickChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String oldNick = event.getOldNick();
        String newNick = event.getNewNick();
        for (int i = 0; i < logins.size(); i++) {
            if (logins.get(i).equals(oldNick)) {
                HBLUser user = logins.remove(i);
                if (user.isTime()) {
                    logins.add(new HBLUser(newNick, user.channel, user.time));
                }
            }
        }
    }

    public boolean isGreeting(String message) {
        for (String greeting : hellos) {
            if (message.contains(greeting)) {
                return true;
            }
        }
        return false;
    }

    private class HBLUser {

        String sender;
        String channel;
        long time;

        public HBLUser(String sen, String chan, long log) {
            sender = sen;
            channel = chan;
            time = log;
        }

        public boolean equals(HBLUser testUser) {
            return (sender.equalsIgnoreCase(testUser.sender) && channel.equalsIgnoreCase(testUser.channel));
        }

        public boolean equals(String sender, String channel) {
            return equals(new HBLUser(sender, channel, 0));
        }

        public boolean equals(String sen) {
            return (sender.equalsIgnoreCase(sen));
        }

        public boolean isTime() {
            return (System.currentTimeMillis() - time < 60 * 1000);
        }
    }
}
