
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Joshua
 */
public class UrbanDictionaryCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        String target = event.getChannel();
        if (target == null) {
            target = event.getSender();
        }
        if (target == null) {
            return;
        }
        BufferedReader reader = null;
        String total = this.buildArgs(event.getArgs());

        try {
            String url = "http://m.urbandictionary.com/#define?term=" + total.replace(" ", "%20");
            URL path = new URL(url);
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
            boolean sent = false;
            for (String string : b) {
                if (string.startsWith("<div class=\"definition\">")) {
                    string = string.replace("<div class=\"definition\">", "").split("</div>")[0];
                    sendMessage(target, total + ": " + string);
                    sent = true;
                    break;
                }
            }
            if (!sent) {
                sendMessage(target, total + ": Nothing found");
            }
        } catch (IOException ex) {
            Logger.getLogger(MCFCommand.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MCFCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "urban",
                    "ur"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
