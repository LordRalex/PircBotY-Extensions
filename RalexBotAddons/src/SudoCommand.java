import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.*;
import com.lordralex.ralexbot.file.FileSystem;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @author Joshua
 */
public class SudoCommand extends Listener {

    Map<String, Boolean> sudoList = new HashMap<>();
    String password;

    public SudoCommand() {
        password = FileSystem.getString("sudo-pass");
    }

    @Override
    public void onNickChange(NickChangeEvent event) {
        Boolean oldValue = sudoList.get(event.getOldNick());
        if (oldValue == null || oldValue == Boolean.FALSE) {
            return;
        }
        sudoList.put(event.getNewNick(), oldValue);
    }

    @Override
    public void onQuit(QuitEvent event) {
        sudoList.remove(event.getQuitter());
    }

    @Override
    public void onPart(PartEvent event) {
        sudoList.remove(event.getSender());
    }

    @Override
    public void onCommand(CommandEvent event) {

        if (event.isCancelled()) {
            return;
        }

        final String sender = event.getSender();
        final String[] args = event.getArgs();

        String[] tells = FileSystem.getTells(sender);
        if (tells.length > 0) {
            getBot().sendNotice(sender, "You have messages waiting for you, *showtells will show you them");
        }

        if (args.length == 0) {
            return;
        }
        if (args[0].equalsIgnoreCase("verify")) {
            if (args[1].equals(password)) {
                sudoList.remove(sender);
                sudoList.put(sender, Boolean.TRUE);
                sendMessage(sender, "Verified");
            }
        } else if (sudoList.get(sender) == Boolean.TRUE) {
            String[] newargs = new String[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                newargs[i - 1] = args[i];
            }
            getBot().manager.runEvent(new CommandEvent(args[0], "Lord_Ralex", event.getChannel(), newargs));
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "sudo"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
