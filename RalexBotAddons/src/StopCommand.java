import com.lordralex.ralexbot.RalexBotMain;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;

/**
 * @version 1.1
 * @author Lord_Ralex
 * @since 1.0
 */
public class StopCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        if (event.getSender() != null) {
            return;
        }

        RalexBotMain.stop();
        String[] channels = getBot().getChannels();
        for (String channel1 : channels) {
            getBot().partChannel(channel1, "Shutting down");
        }
        getBot().disconnect();
        getBot().quitServer();
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "stop"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
