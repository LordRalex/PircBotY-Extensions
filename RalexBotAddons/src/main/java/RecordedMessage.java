
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.settings.Settings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class RecordedMessage extends Listener {

    final Map<String, Thread> threads = new ConcurrentHashMap<>();
    ;
    int counter = 0;

    @Override
    @EventType(event = EventField.Message, priority = Priority.LOW)
    public void runEvent(MessageEvent event) {
        if (!Utils.hasOP(event.getSender(), event.getChannel())) {
            return;
        }
        if (event.getMessage().startsWith(Utils.getNick() + ", please teach")) {
            String load = event.getMessage().replace(Utils.getNick() + ", please teach", "").trim();
            load = load.split(" ")[0];
            AutomatedMessageThread thread = new AutomatedMessageThread(load, event.getChannel(), counter);
            counter++;
            threads.put(thread.getName(), thread);
            thread.start();
            return;
        } else if (event.getMessage().startsWith(Utils.getNick() + ", please stop")) {
            String load = event.getMessage().replace(Utils.getNick() + ", please teach", "").trim();
            load = load.split(" ")[0];
            if (load.equalsIgnoreCase("all")) {
                Utils.sendMessage(event.getChannel(), "Stopping all then");
                Set<String> ids = threads.keySet();
                for (String id : ids) {
                    threads.remove(id).interrupt();
                }
                Utils.sendMessage(event.getChannel(), "All stopped");
            } else {
                Thread thread = threads.remove(load);
                if (thread == null) {
                    Utils.sendMessage(event.getChannel(), "No thread with that name");
                } else {
                    thread.interrupt();
                }
            }
        }
    }

    private class AutomatedMessageThread extends Thread {

        final List<String> messages;
        final String chan;
        final int message_delay;
        boolean stop = false;

        public AutomatedMessageThread(String section, String channel, int ID) {
            super("message_" + ID);
            Utils.sendMessage(channel, "Loading " + section);
            messages = Settings.getGlobalSettings().getStringList(section);
            chan = channel;
            message_delay = Settings.getGlobalSettings().getInt(section + "_timing");
            Utils.sendMessage(channel, "Loaded with name " + this.getName());
        }

        @Override
        public void run() {
            while (!isInterrupted() && !messages.isEmpty()) {
                synchronized (this) {
                    try {
                        wait(message_delay * 1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace(System.out);
                    }
                }
                if (!isInterrupted() && !messages.isEmpty()) {
                    synchronized (messages) {
                        String nextLine = messages.remove(0);
                        Utils.sendMessage(chan, nextLine);
                    }
                }
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
            stop = true;
        }

        @Override
        public boolean isInterrupted() {
            if (super.isInterrupted()) {
                return true;
            } else if (stop) {
                return true;
            } else {
                return false;
            }
        }
    }
}
