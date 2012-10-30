
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeadflyCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        final String sender = event.getSender();
        final String[] args = event.getArgs();
        final String channel = event.getChannel();
        BufferedReader reader = null;
        String target = channel;
        if (target == null) {
            target = sender;
        }
        if (target == null) {
            return;
        }
        if (args.length == 0) {
            Utils.sendMessage(target, "*deadfly <link>");
            return;
        }
        try {
            String url = args[0].replace(" ", "%20");
            URL path = new URL(url);
            reader = new BufferedReader(new InputStreamReader(path.openStream()));
            List<String> parts = new ArrayList<String>();
            String s;
            while ((s = reader.readLine()) != null) {
                parts.add(s);
            }
            List<String> b = new ArrayList<String>();
            for (String part : parts) {
                String[] c = part.split(",");
                b.addAll(Arrays.asList(c));
            }
            for (String string : b) {
                string = string.trim();
                if (string.startsWith("var url")) {
                    string = string.replace("var url =", "");
                    string = string.replace("\'", "");
                    string = string.replace(";", "");
                    string = string.trim();
                    if (!string.startsWith("https://adf.ly/")) {
                        string = "https://adf.ly/" + string;
                    }
                    Utils.sendMessage(target, getLink(string));
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MCFCommand.class.getName()).log(Level.SEVERE, null, ex);
            Utils.sendMessage(target, "There was a problem handling the link");
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
                    "deadfly",
                    "df"
                };
    }

    private String getLink(String u) throws IOException {
        URL url = new URL(u);
        HttpURLConnection newC = (HttpURLConnection) url.openConnection();
        DataInputStream reader = new DataInputStream(newC.getInputStream());
        List<String> lines = new ArrayList<String>();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
        } catch (EOFException e) {
            reader.close();
        }

        String result = url.toExternalForm();

        for (String line : lines) {
            if (line.startsWith("<META")) {
                result = line.split("URL=")[1].replace("\"", "").replace(">", "");
            }
        }
        return result;
    }
}
