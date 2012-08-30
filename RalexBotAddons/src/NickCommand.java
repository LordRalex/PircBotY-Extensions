import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class NickCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String sender = event.getSender(), channel = event.getChannel();
        String[] args = event.getArgs();
        if (!isMaster(sender) && !isOP(sender, channel)) {
            return;
        }
        String newNick;
        if (args.length == 1) {
            newNick = args[0];
        } else {
            newNick = "RalexBot";
        }
        getPircBot().changeNick(newNick);
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "nick"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
