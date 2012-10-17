
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.settings.Settings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TellCommand extends Listener {

    Map<String, Long> lastTold = new ConcurrentHashMap<String, Long>();

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        final String sender = event.getSender();

        String[] tells;
        try {
            tells = getTells(sender);
        } catch (FileNotFoundException ex) {
            return;
        }
        if (tells.length > 0) {
            Long timeAgo = lastTold.get(event.getSender());
            if (timeAgo == null || System.currentTimeMillis() - timeAgo.longValue() > Settings.getInt("refresh-minutes") * 1000 * 60) {
                lastTold.put(event.getSender(), System.currentTimeMillis());
                Utils.sendNotice(sender, "You have messages waiting for you, *st will show you them");
            }
        }
    }

    @Override
    @EventType(event = EventField.NickChange)
    public void runEvent(NickChangeEvent event) {
        String sender = event.getNewNick();
        String[] tells;
        try {
            tells = getTells(sender);
        } catch (FileNotFoundException ex) {
            return;
        }
        Long timeAgo = lastTold.get(event.getOldNick());
        if (timeAgo != null) {
            lastTold.put(event.getNewNick(), timeAgo);
        }
        if (tells.length > 0) {
            if (timeAgo == null || System.currentTimeMillis() - timeAgo.longValue() > Settings.getInt("refresh-minutes") * 1000 * 60) {
                lastTold.put(event.getNewNick(), System.currentTimeMillis());
                Utils.sendNotice(sender, "You have messages waiting for you, *st will show you them");
            }
        }
    }

    @Override
    @EventType(event = EventField.Message)
    public void runEvent(MessageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final String sender = event.getSender();
        String[] tells;
        try {
            tells = getTells(sender);
        } catch (FileNotFoundException ex) {
            return;
        }
        if (tells.length > 0) {
            Long timeAgo = lastTold.get(event.getSender());
            if (timeAgo == null || System.currentTimeMillis() - timeAgo.longValue() > Settings.getInt("refresh-minutes") * 1000 * 60) {
                lastTold.put(event.getSender(), System.currentTimeMillis());
                Utils.sendNotice(sender, "You have messages waiting for you, *st will show you them");
            }
        }
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        String channel = event.getChannel();
        String sender = event.getSender();
        String[] args = event.getArgs();
        String command = event.getCommand();
        if (sender == null || sender.isEmpty()) {
            return;
        }
        if (command.equalsIgnoreCase("t") || command.equalsIgnoreCase("tell")) {
            String message = "";
            for (int i = 1; i < args.length; i++) {
                message += args[i] + " ";
            }
            message = message.trim();
            String target = args[0];
            try {
                addTell(sender, target, message);
            } catch (IOException ex) {
                Logger.getLogger(TellCommand.class.getName()).log(Level.SEVERE, null, ex);
                if (channel != null) {
                    Utils.sendMessage(channel, "An error occurred, get Lord_Ralex to see what went wrong");
                } else if (sender != null) {
                    Utils.sendMessage(sender, "An error occurred, get Lord_Ralex to see what went wrong");
                }
                return;
            }
            if (channel != null) {
                Utils.sendMessage(channel, "Message will be delivered to " + target);
            } else if (sender != null) {
                Utils.sendMessage(sender, "Message will be delivered to " + target);
            }
        } else if (command.equalsIgnoreCase("st") || command.equalsIgnoreCase("showtells")) {
            if (sender == null) {
                return;
            }
            String[] messages = null;
            try {
                messages = getTells(sender);
            } catch (FileNotFoundException ex) {
            }
            if (messages == null || messages.length == 0) {
                Utils.sendNotice(sender, "I have no messages for you");
            } else {
                Utils.sendNotice(sender, messages);
            }
            clearTells(sender);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "tell",
                    "t",
                    "showtells",
                    "st"
                };
    }

    public void saveTells(String name, String[] lines) {
        if (name == null || lines == null || name.length() == 0) {
            return;
        }
        name = name.toLowerCase();
        new File("data" + File.separator + "tells").mkdirs();
        new File("data" + File.separator + "tells" + File.separator + name + ".txt").delete();
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File("data" + File.separator + "tells" + File.separator + name + ".txt"));
            for (String line : lines) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(TellCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void clearTells(String name) {
        saveTells(name, new String[0]);
    }

    public String[] getTells(String name) throws FileNotFoundException {
        if (name == null || name.length() == 0) {
            return new String[0];
        }
        name = name.toLowerCase();
        List<String> lines = new ArrayList<String>();
        Scanner reader = new Scanner(new File("data" + File.separator + "tells" + File.separator + name + ".txt"));
        while (reader.hasNext()) {
            lines.add(reader.nextLine().trim());
        }
        String[] result = lines.toArray(new String[0]);
        return result;
    }

    public void addTell(String sender, String target, String message) throws IOException {
        if (target == null || sender == null || message == null) {
            return;
        }
        List<String> lines = new ArrayList<String>();
        String[] old;
        try {
            old = getTells(target);
        } catch (FileNotFoundException ex) {
            old = new String[0];
        }
        lines.addAll(Arrays.asList(old));
        lines.add("From " + sender + "-> " + message);
        saveTells(target, lines.toArray(new String[0]));
    }
}
