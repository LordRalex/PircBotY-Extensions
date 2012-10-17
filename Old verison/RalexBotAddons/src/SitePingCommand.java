import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.0
 * @author Joshua
 */
public class SitePingCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final String channel = event.getChannel();
        final String sender = event.getSender();
        final String[] args = event.getArgs();
        final String command = event.getCommand();

        URL site;
        String target = channel;
        if (target == null) {
            target = sender;
        }
        if (target == null) {
            return;
        }
        if (command.equalsIgnoreCase("mcf")) {
            try {
                site = new URL("http://www.minecraftforum.net");
            } catch (MalformedURLException ex) {
                Logger.getLogger(SitePingCommand.class.getName()).log(Level.SEVERE, null, ex);
                sendMessage(target, "Seems as though I could not make that an url >.>");
                return;
            }
        } else {
            if (args.length != 1) {
                sendMessage(target, "The correct usage is *pingsite <url>");
                return;
            }
            try {
                String path = args[0];
                if (!path.startsWith("http://") && !path.startsWith("https://")) {
                    path = "http://" + path;
                }
                site = new URL(path);
            } catch (MalformedURLException ex) {
                Logger.getLogger(SitePingCommand.class.getName()).log(Level.SEVERE, null, ex);
                sendMessage(target, "Seems as though I could not make that an url >.>");
                return;
            }
        }
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) site.openConnection();
            connection.setReadTimeout(5000);
            int code = connection.getResponseCode();
            if (code == 200) {
                sendMessage(target, "I could reach that site just fine");
            } else {
                sendMessage(target, "I got response code " + code);
            }
        } catch (SocketTimeoutException ex) {
            sendMessage(target, "I timed out trying to reach that site");
        } catch (IOException ex) {
            Logger.getLogger(SitePingCommand.class.getName()).log(Level.SEVERE, null, ex);
            sendMessage(target, "I was unable to work this out, so I cannot reach the site >.>");
        }

    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "pingsite",
                    "pingmcf"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
