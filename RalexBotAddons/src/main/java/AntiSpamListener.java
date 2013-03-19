
import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.events.ActionEvent;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.users.BotUser;
import com.lordralex.ralexbot.api.users.User;
import com.lordralex.ralexbot.settings.Settings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiSpamListener extends Listener {

    private final Map<String, Posts> logs = new HashMap<>();
    //private final Map<String, String> joinLeaveMap = new HashMap<>();
    private int MAX_MESSAGES;
    private int SPAM_RATE;
    private int DUPE_RATE;
    private final Settings settings = Settings.getGlobalSettings();
    private final List<String> channels = new ArrayList<>();

    @Override
    public void setup() {
        MAX_MESSAGES = settings.getInt("spam-message");
        SPAM_RATE = settings.getInt("spam-time");
        DUPE_RATE = settings.getInt("spam-dupe");
        channels.clear();
        channels.addAll(settings.getStringList("spam-channels"));
    }

    @Override
    @EventType(event = EventField.Message, priority = Priority.LOW)
    public void runEvent(MessageEvent event) {
        synchronized (logs) {
            Channel channel = event.getChannel();
            if (!channels.contains(channel.getName().toLowerCase())) {
                return;
            }
            User sender = event.getSender();
            String message = event.getMessage();
            if (sender.hasOP(channel.getName()) || sender.hasVoice(channel.getName()) || sender.getNick().equalsIgnoreCase(BotUser.getBotUser().getNick())) {
                return;
            }
            message = message.toString().toLowerCase();
            Posts posts = logs.remove(sender.getNick());
            if (posts == null) {
                posts = new Posts();
            }
            if (posts.addPost(message)) {
                if (RalexBot.getDebugMode()) {
                    BotUser.getBotUser().sendMessage(Settings.getGlobalSettings().getString("debug-channel"),
                            "Would have kicked " + event.getSender().getNick() + " with last line of " + posts.posts.get(posts.posts.size() - 1));
                } else {
                    BotUser.getBotUser().kick(sender.getNick(), channel.getName(), "Triggered Spam Guard (IP=" + sender.getIP() + ")");
                    //sender.quiet(event.getChannel());
                    //UnquietThread quiet = new UnquietThread(event.getChannel().getName(), sender.getQuietLine(), 10 * 1000);
                }
                event.setCancelled(true);
            } else {
                logs.put(sender.getNick(), posts);
            }
        }
    }

    @Override
    @EventType(event = EventField.Action, priority = Priority.LOW)
    public void runEvent(ActionEvent event) {
        synchronized (logs) {
            if (event.isCancelled()) {
                return;
            }
            Channel channel = event.getChannel();
            User sender = event.getSender();
            String message = event.getAction();
            if (sender.hasOP(channel.getName()) || sender.hasVoice(channel.getName()) || sender.getNick().equalsIgnoreCase(BotUser.getBotUser().getNick())) {
                return;
            }
            message = message.toString().toLowerCase();
            Posts posts = logs.remove(sender.getNick());
            if (posts == null) {
                posts = new Posts();
            }
            if (posts.addPost(message)) {
                if (RalexBot.getDebugMode()) {
                    BotUser.getBotUser().sendMessage(Settings.getGlobalSettings().getString("debug-channel"),
                            "Would have kicked " + event.getSender().getNick() + " with last line of " + posts.posts.get(posts.posts.size() - 1));
                } else {
                    //BotUser.getBotUser().kick(sender.getNick(), channel.getName(), "Triggered Spam Guard (IP=" + sender.getIP() + ")");
                }
                event.setCancelled(true);
            } else {
                logs.put(sender.getNick(), posts);
            }
        }
    }

    private class Posts {

        List<Post> posts = new ArrayList<>();

        public boolean addPost(String lastPost) {
            posts.add(new Post(System.currentTimeMillis(), lastPost));
            if (posts.size() == MAX_MESSAGES) {
                boolean areSame = true;
                for (int i = 1; i < posts.size() && areSame; i++) {
                    if (!posts.get(i - 1).message.equalsIgnoreCase(posts.get(i).message)) {
                        areSame = false;
                    }
                }
                if (areSame) {
                    if (posts.get(posts.size() - 1).getTime() - posts.get(0).getTime() < DUPE_RATE) {
                        return true;
                    }
                }
                if (posts.get(posts.size() - 1).getTime() - posts.get(0).getTime() < SPAM_RATE) {
                    return true;
                }
                posts.remove(0);
            }
            return false;
        }
    }

    private class Post {

        long timePosted;
        String message;

        public Post(long Time, String Message) {
            timePosted = Time;
            message = Message;
        }

        public String getMessage() {
            return message;
        }

        public long getTime() {
            return timePosted;
        }
    }
}
