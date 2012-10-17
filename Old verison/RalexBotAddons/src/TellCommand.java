
import com.lordralex.ralexbot.RalexBotMain;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.file.FileSystem;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class TellCommand extends Listener {

    Map<String, Long> lastTold = new ConcurrentHashMap<String, Long>();

    @Override
    public void onJoin(JoinEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final String sender = event.getSender();

        String[] tells;
        try {
            tells = FileSystem.getTells(sender);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TellCommand.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        if (tells.length > 0) {
            Long timeAgo = lastTold.get(event.getSender());
            if (timeAgo == null || System.currentTimeMillis() - timeAgo.longValue() > FileSystem.getInt("refresh-minutes") * 1000 * 60) {
                lastTold.put(event.getSender(), System.currentTimeMillis());
                getPircBot().sendNotice(sender, "You have messages waiting for you, *st will show you them");
            }
        }
    }

    @Override
    public void onNickChange(NickChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        String sender = event.getNewNick();

        String[] tells;
        try {
            tells = FileSystem.getTells(sender);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TellCommand.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        Long timeAgo = lastTold.get(event.getOldNick());
        if (timeAgo != null) {
            lastTold.put(event.getNewNick(), timeAgo);
        }
        if (tells.length > 0) {
            if (timeAgo == null || System.currentTimeMillis() - timeAgo.longValue() > FileSystem.getInt("refresh-minutes") * 1000 * 60) {
                lastTold.put(event.getNewNick(), System.currentTimeMillis());
                getPircBot().sendNotice(sender, "You have messages waiting for you, *st will show you them");
            }
        }
    }

    @Override
    public void onMessage(MessageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final String sender = event.getSender();

        String[] tells;
        try {
            tells = FileSystem.getTells(sender);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TellCommand.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        if (tells.length > 0) {
            Long timeAgo = lastTold.get(event.getSender());
            if (timeAgo == null || System.currentTimeMillis() - timeAgo.longValue() > FileSystem.getInt("refresh-minutes") * 1000 * 60) {
                lastTold.put(event.getSender(), System.currentTimeMillis());
                getPircBot().sendNotice(sender, "You have messages waiting for you, *st will show you them");
            }
        }
    }

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

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
                FileSystem.addTell(sender, target, message);
            } catch (IOException ex) {
                Logger.getLogger(TellCommand.class.getName()).log(Level.SEVERE, null, ex);
                if (channel != null) {
                    sendMessage(channel, "An error occurred, get Lord_Ralex to see what went wrong");
                } else if (sender != null) {
                    sendMessage(sender, "An error occurred, get Lord_Ralex to see what went wrong");
                }
                return;
            }
            if (channel != null) {
                sendMessage(channel, "Message will be delivered to " + target);
            } else if (sender != null) {
                sendMessage(sender, "Message will be delivered to " + target);
            }
        } else if (command.equalsIgnoreCase("st") || command.equalsIgnoreCase("showtells")) {
            if (sender == null) {
                return;
            }
            String[] messages = null;
            try {
                messages = FileSystem.getTells(sender);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TellCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (messages == null || messages.length == 0) {
                sendNotice(sender, "I have no messages for you");
            } else {
                sendNotice(sender, messages);
            }
            try {
                FileSystem.clearTells(sender);
            } catch (IOException ex) {
                Logger.getLogger(TellCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
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

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
        priorities.put(EventType.Join, Priority.NORMAL);
        priorities.put(EventType.NickChange, Priority.NORMAL);
    }
}
