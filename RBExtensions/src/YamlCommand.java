
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YamlCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        final String channel = event.getChannel();
        final String sender = event.getSender();
        final String[] args = event.getArgs();

        if (args.length != 1) {
            if (channel != null) {
                Utils.sendMessage(channel, "Usage: *yaml <link>");
            } else if (sender != null) {
                Utils.sendMessage(sender, "Usage: *yaml <link>");
            }
            return;
        }
        String url = args[0];
        if (url.startsWith("http://www.pastebin.com/") || url.startsWith("www.pastebin.com/") || url.startsWith("pastebin.com/") || url.startsWith("http://pastebin.com/")) {
            if (!url.contains("raw.php?i=")) {
                String[] parts = url.split("/");
                String id = parts[parts.length - 1];
                url = "http://www.pastebin.com/raw.php?i=" + id;
                System.out.println("New url: " + url);
            }
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            List<String> lines = new ArrayList<>();
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
            List<String> errorLog = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("\t")) {
                    errorLog.add("On line " + (i + 1) + ", you have a tab.");
                }
                {
                    int counter = 0;
                    for (char ch : line.toCharArray()) {
                        if (ch == '\'') {
                            counter++;
                        }
                    }
                    if (counter != 2) {
                        if (counter == 1) {
                            errorLog.add("On line " + (i + 1) + ", you did not put enough 's");
                        }
                        if (counter > 2) {
                            errorLog.add("On line " + (i + 1) + ", you have too many 's");
                        }
                    }
                }
            }
            if (errorLog.size() > 10) {
                if (channel != null) {
                    Utils.sendMessage(channel, "Your file has over 10 errors. Here are the first 10");
                } else if (sender != null) {
                    Utils.sendMessage(sender, "Your file has over 10 errors. Here are the first 10");
                }
            }
            int maxLines = 5;
            if (errorLog.size() < maxLines) {
                maxLines = errorLog.size();
            }
            if (maxLines == 0) {
                if (channel != null) {
                    Utils.sendMessage(channel, "File seems to be fine");
                } else if (sender != null) {
                    Utils.sendMessage(sender, "File seems to be fine");
                }
            }
            for (int i = 0; i < maxLines; i++) {
                if (sender != null) {
                    Utils.sendMessage(sender, errorLog.get(i));
                }
            }
        } catch (IOException e) {
            if (channel != null) {
                Utils.sendMessage(channel, "Error in reading file");
            } else if (sender != null) {
                Utils.sendMessage(sender, "Error in reading file");
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
        }

    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "yaml"
                };
    }
}
