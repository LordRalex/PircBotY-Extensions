
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.settings.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiSpamListener extends Listener {

    private final Map<String, Posts> logs = new HashMap<>();
    private int MAX_MESSAGES;
    private int SPAM_RATE;
    private int DUPE_RATE;
    private Settings settings;

    @Override
    public void setup() {
        settings = new Settings(new File("settings", "config.yml"));
        MAX_MESSAGES = settings.getInt("spam-message");
        SPAM_RATE = settings.getInt("spam-time");
        DUPE_RATE = settings.getInt("spam-dupe");
    }

    @Override
    @EventType(event = EventField.Message, priority = Priority.LOW)
    public void runEvent(MessageEvent event) {
        synchronized (logs) {
            if (event.isCancelled()) {
                return;
            }
            String channel = event.getChannel();
            String sender = event.getSender();
            String message = event.getMessage();
            String hostname = event.getHostname();
            if (Utils.hasOP(sender, channel) || Utils.hasVoice(sender, channel) || sender.equalsIgnoreCase(Utils.getNick())) {
                return;
            }
            message = message.toString().toLowerCase();
            Posts posts = logs.remove(sender);
            if (posts == null) {
                posts = new Posts();
            }
            if (posts.addPost(message)) {
                Utils.kick(sender, channel, "Triggered Spam Guard (IP=" + hostname + ")");
                event.setCancelled(true);
            } else {
                logs.put(sender, posts);
            }
        }
    }

    private class Posts {

        List<Post> posts = new ArrayList<>();

        public boolean addPost(String lastPost) {
            posts.add(new Post(System.currentTimeMillis(), lastPost));
            if (posts.size() == MAX_MESSAGES) {
                posts.remove(0);
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
