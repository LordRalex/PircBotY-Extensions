
import com.lordralex.ralexbot.RalexBotMain;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.events.PartEvent;
import com.lordralex.ralexbot.api.events.QuitEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
public class SeenCommand extends Listener {

    File seen;

    public SeenCommand() {
        seen = new File("data" + File.separator + "seen");
    }

    @Override
    public void onCommand(CommandEvent event) {
        updateTimes(event.getSender(), event.getChannel());
    }

    @Override
    public void onMessage(MessageEvent event) {
    }

    @Override
    public void onJoin(JoinEvent event) {
    }

    @Override
    public void onPart(PartEvent event) {
    }

    @Override
    public void onQuit(QuitEvent event) {
    }

    private void updateTimes(String name, String channel) {
    }

    private void write(Last lastSeen) {
        FileWriter writer = null;
        try {
            File file = new File(seen, lastSeen.name.toLowerCase() + "-" + lastSeen.channel + ".txt");
            if (file.exists()) {
                file.delete();
            }
            writer = new FileWriter(file);
            writer.write(lastSeen.timeago + "\n");
        } catch (IOException ex) {
            Logger.getLogger(SeenCommand.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(SeenCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
        priorities.put(EventType.Message, Priority.NORMAL);
        priorities.put(EventType.Join, Priority.NORMAL);
        priorities.put(EventType.Part, Priority.NORMAL);
        priorities.put(EventType.Quit, Priority.NORMAL);
    }

    private class Last {

        String name;
        String channel;
        long timeago;
    }
}
