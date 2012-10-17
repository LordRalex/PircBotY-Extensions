
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;

/**
 *
 * @author Joshua
 */
public class Logger extends Listener {

    PrintStream logs;

    @Override
    public void setup() {
        new File("logs").mkdirs();
        try {
            logs = new PrintStream(new File("logs", "commands.log"));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onCommand(CommandEvent event) {
        logs.append(event.getSender() + " used " + event.getCommand() + " " + buildArgs(event.getArgs()) + "\r");
        logs.flush();
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.HIGHEST);
    }
}
