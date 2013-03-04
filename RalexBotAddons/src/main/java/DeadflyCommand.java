
import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.sender.Sender;
import com.lordralex.ralexbot.api.users.User;
import com.lordralex.ralexbot.settings.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeadflyCommand extends Listener {

    private String adflyLine;

    @Override
    public void setup() {
        adflyLine = Settings.getGlobalSettings().getString("adfly");
        if (adflyLine == null || adflyLine.isEmpty()) {
            adflyLine = "var zzz";
        }
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final User sender = event.getSender();
        final String[] args = event.getArgs();
        final Channel channel = event.getChannel();
        BufferedReader reader = null, redirectReader = null;
        URL path = null;
        Sender target = channel;
        if (target == null) {
            target = sender;
        }
        if (target == null) {
            return;
        }
        if (args.length == 0) {
            target.sendMessage("*deadfly <link>");
            return;
        }
        String reply = "";
        try {
            String url = args[0].replace(" ", "%20");
            path = new URL(url);
            reader = new BufferedReader(new InputStreamReader(path.openStream()));
            List<String> parts = new ArrayList<>();
            String s;
            while ((s = reader.readLine()) != null) {
                parts.add(s);
            }
            List<String> b = new ArrayList<>();
            for (String part : parts) {
                String[] c = part.split(",");
                b.addAll(Arrays.asList(c));
            }
            String forward = null;
            for (String string : b) {
                string = string.trim();
                if (string.startsWith("var zzz")) {
                    string = string.replace("var zzz =", "");
                    string = string.replace("\'", "");
                    string = string.replace(";", "");
                    string = string.trim();
                    if (!string.startsWith("https://adf.ly/")) {
                        forward = "https://adf.ly" + string;
                    } else {
                        forward = string;
                    }
                }
            }
            reply = forward;
        } catch (IOException ex) {
            RalexBot.getLogger().log(Level.SEVERE, null, ex);
            reply = "There was a problem handling the link";
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                RalexBot.getLogger().log(Level.SEVERE, null, ex);
            }
            try {
                if (redirectReader != null) {
                    redirectReader.close();
                }
            } catch (IOException ex) {
                RalexBot.getLogger().log(Level.SEVERE, null, ex);
            }
        }
        target.sendMessage(reply);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "deadfly",
            "df"
        };
    }
}
