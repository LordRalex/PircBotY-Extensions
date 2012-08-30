
import com.lordralex.ralexbot.RalexBotMain;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import java.util.Set;
import org.pircbotx.Channel;

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
        Set<Channel> channels = getPircBot().getChannels();
        for (Channel channel1 : channels) {
            getPircBot().partChannel(channel1, "Shutting down");
        }
        getPircBot().disconnect();
        getPircBot().quitServer();
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
