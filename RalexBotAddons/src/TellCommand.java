
import com.lordralex.ralexbot.RalexBotMain;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.file.FileSystem;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class TellCommand extends Listener {

    @Override
    public void onJoin(JoinEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final String sender = event.getSender();

        String[] tells = FileSystem.getTells(sender);
        if (tells.length > 0) {
            getBot().sendNotice(sender, "You have messages waiting for you, *showtells will show you them");
        }
    }

    @Override
    public void onNickChange(NickChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        String sender = event.getNewNick();

        String[] tells = FileSystem.getTells(sender);
        if (tells.length > 0) {
            getBot().sendNotice(sender, "You have messages waiting for you, *showtells will show you them");
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
            FileSystem.addTell(sender, target, message);
            if (channel != null) {
                sendMessage(channel, "Message will be delivered to " + target);
            } else if (sender != null) {
                sendMessage(sender, "Message will be delivered to " + target);
            } else {
                RalexBotMain.print("Message will be delivered to " + target);
            }
        } else if (command.equalsIgnoreCase("st") || command.equalsIgnoreCase("showtells")) {
            if (sender == null) {
                return;
            }
            String[] messages = FileSystem.getTells(sender);
            if (messages.length == 0) {
                sendMessage(sender, "I have no messages for you");
            } else {
                sendMessage(sender, messages);
            }
            FileSystem.clearTells(sender);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "tell",
                    "t",
                    "showtells",
                    "st",
                    "savetell"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
        priorities.put(EventType.Join, Priority.NORMAL);
        priorities.put(EventType.NickChange, Priority.NORMAL);
    }
}
