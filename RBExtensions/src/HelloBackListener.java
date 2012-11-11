
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.api.events.PartEvent;
import com.lordralex.ralexbot.api.events.QuitEvent;
import com.lordralex.ralexbot.settings.Settings;
import java.util.ArrayList;
import java.util.List;

public class HelloBackListener extends Listener {

    List<User> logins = new ArrayList<>();
    List<String> hellos = new ArrayList<>();

    @Override
    public void setup() {
        List<String> more = Settings.getStringList("greetings");
        if (more != null && !more.isEmpty()) {
            for (String string : more) {
                hellos.add(string.toLowerCase());
            }
        }
    }

    @Override
    @EventType(event = EventField.Message)
    public void runEvent(MessageEvent event) {
        String message = event.getMessage();
        String channel = event.getChannel();
        String sender = event.getSender();
        if (message.equalsIgnoreCase("Hello " + Utils.getNick()) || message.equalsIgnoreCase("Hello, " + Utils.getNick())
                || message.equalsIgnoreCase("Hi, " + Utils.getNick()) || message.equalsIgnoreCase("Hi " + Utils.getNick())) {
            Utils.sendMessage(channel, "Why hello there " + sender + ", thank you for telling me hello. I <3 you");
        } else if (isGreeting(message)) {
            for (int i = 0; i < logins.size(); i++) {
                if (logins.get(i).equals(sender, channel)) {
                    User user = logins.remove(i);
                    i--;
                    if (user.isTime()) {
                        Utils.sendMessage(user.channel, "Hello " + user.sender);
                    }
                }
            }
        }
    }

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        String sender = event.getSender();
        String channel = event.getChannel();
        logins.add(new User(sender, channel, System.currentTimeMillis()));
    }

    @Override
    @EventType(event = EventField.Part)
    public void runEvent(PartEvent event) {
        String sender = event.getSender();
        String channel = event.getChannel();
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
        String sourceNick = event.getSender();
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
                User user = logins.remove(i);
                if (user.isTime()) {
                    logins.add(new User(newNick, user.channel, user.time));
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

    private class User {

        String sender;
        String channel;
        long time;

        public User(String sen, String chan, long log) {
            sender = sen;
            channel = chan;
            time = log;
        }

        public boolean equals(User testUser) {
            return (sender.equalsIgnoreCase(testUser.sender) && channel.equalsIgnoreCase(testUser.channel));
        }

        public boolean equals(String sender, String channel) {
            return equals(new User(sender, channel, 0));
        }

        public boolean equals(String sen) {
            return (sender.equalsIgnoreCase(sen));
        }

        public boolean isTime() {
            return (System.currentTimeMillis() - time < 60 * 1000);
        }
    }
}
