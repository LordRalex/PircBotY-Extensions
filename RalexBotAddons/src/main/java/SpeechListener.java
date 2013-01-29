
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.users.BotUser;
import com.lordralex.ralexbot.api.users.User;
import com.lordralex.ralexbot.settings.Settings;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpeechListener extends Listener {

    Map<String, String> listings = new ConcurrentHashMap<>();

    @Override
    public void setup() {
        Settings settings = new Settings(new File("settings", "config.yml"));
        List<String> lines = settings.getStringList("replies");
        if (lines != null && !lines.isEmpty()) {
            for (String line : lines) {
                try {
                    int split = line.indexOf("|");
                    if (split == -1) {
                        continue;
                    }
                    String key = line.substring(0, split).trim();
                    String value = line.substring(split + 1).trim();
                    if (key.isEmpty() || value.isEmpty()) {
                        continue;
                    }
                    listings.put(key, value);
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }
    }

    @Override
    public void runEvent(MessageEvent event) {
        for (String key : listings.keySet()) {
            if (event.getMessage().startsWith(key)) {
                String value = listings.get(key);
                int split = value.indexOf("|");
                Rank required;
                if (split == -1) {
                    required = Rank.MEMBER;
                } else {
                    required = Rank.valueOf(value.substring(split + 1).trim().toUpperCase());
                }
                if (required == Rank.OWNER) {
                    return;
                } else if (required == Rank.OP) {
                    if (!event.getSender().hasOP(event.getChannel().getName())) {
                        return;
                    }
                } else if (required == Rank.VOICE) {
                    if (!event.getSender().hasOP(event.getChannel().getName()) && !event.getSender().hasVoice(event.getChannel().getName())) {
                        return;
                    }
                }
                String reply = value;
                if (split != -1) {
                    reply = value.substring(0, split).trim();
                }
                if (reply.startsWith("/")) {

                    User user = null;
                    if (reply.contains("{User}")) {
                        user = User.getUser("");
                    }

                    if (reply.startsWith("/kickban")) {
                        if (user == null) {
                            return;
                        }
                    } else if (reply.startsWith("/ban")) {
                        if (user == null) {
                            return;
                        }
                    } else if (reply.startsWith("/kick")) {
                        if (user == null) {
                            return;
                        }
                    }
                } else {
                    BotUser.getBotUser().sendMessage(event.getChannel().getName(), reply);
                }
                break;
            }
        }
    }

    private enum Rank {

        MEMBER,
        VOICE,
        OP,
        OWNER
    }
}
