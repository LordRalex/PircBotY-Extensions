import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0
 * @author Joshua
 */
public class HelloBackListener extends Listener {

    List<User> logins = new ArrayList<User>();
    List<String> hellos = new ArrayList<String>();

    public HelloBackListener() {
        hellos.add("hello");
        hellos.add("hi");
        hellos.add("o/");
        hellos.add("greetings");
        hellos.add("allo");
        hellos.add("is anyone here");
        hellos.add("anyone here");
    }

    @Override
    public void onMessage(MessageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String message = event.getMessage();
        String channel = event.getChannel();
        String sender = event.getSender();
        if (message.equalsIgnoreCase("Hello " + getPircBot().getNick()) || message.equalsIgnoreCase("Hello, " + getPircBot().getNick())) {
            sendMessage(channel, "Why hello there " + sender + ", thank you for telling me hi. I <3 you");
        } else if (isGreeting(message)) {
            for (int i = 0; i < logins.size(); i++) {
                if (logins.get(i).equals(sender, channel)) {
                    User user = logins.remove(i);
                    i--;
                    if (user.isTime()) {
                        sendMessage(user.channel, "Hello " + user.sender);
                    }
                }
            }
        }
    }

    @Override
    public void onJoin(JoinEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String sender = event.getSender();
        String channel = event.getChannel();
        logins.add(new User(sender, channel, System.currentTimeMillis()));
    }

    @Override
    public void onPart(PartEvent event) {
        if (event.isCancelled()) {
            return;
        }
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
    public void onQuit(QuitEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String sourceNick = event.getQuitter();
        for (int i = 0; i < logins.size(); i++) {
            if (logins.get(i).equals(sourceNick)) {
                logins.remove(i);
                i--;
            }
        }
    }

    @Override
    public void onNickChange(NickChangeEvent event) {
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
        String[] parts = message.toLowerCase().trim().split(" ");
        for (String messagePart : parts) {
            for (String greeting : hellos) {
                if (messagePart.equalsIgnoreCase(greeting)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String[] args = event.getArgs();
        String channel = event.getChannel();
        String sender = event.getSender();
        if (args.length != 0) {
            String newHello = "";
            for (String string : args) {
                newHello += string + " ";
            }
            newHello = newHello.trim().toLowerCase();
            hellos.add(newHello);
            if (channel != null) {
                sendMessage(channel, "Added new response: " + newHello);
            } else if (sender != null) {
                sendMessage(sender, "Added new response: " + newHello);
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "addhello"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
        priorities.put(EventType.NickChange, Priority.NORMAL);
        priorities.put(EventType.Quit, Priority.NORMAL);
        priorities.put(EventType.Part, Priority.NORMAL);
        priorities.put(EventType.Join, Priority.NORMAL);
        priorities.put(EventType.Message, Priority.NORMAL);
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
