
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;



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
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        logs.append(event.getSender() + " used " + event.getCommand() + " " + Utils.toString(event.getArgs()) + "\r");
        logs.flush();
    }
}
