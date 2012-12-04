
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import java.util.Random;

public class ChooseCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        String target = event.getChannel();
        if (target == null) {
            target = event.getSender();
        }
        if (target == null) {
            return;
        }

        String[] args = event.getArgs();
        if (args.length == 0) {
            Utils.sendMessage(target, "Command use: *choose [choices] (you can use spaces or , to separate them)");
            return;
        }

        String total = Utils.toString(args);

        String[] choices;
        if (total.contains(",")) {
            choices = total.split(",");
        } else {
            choices = total.split(" ");
        }
        if (choices.length <= 1) {
            Utils.sendMessage(target, "What kind of a choice is that?");
            return;
        }

        String answer = choices[new Random().nextInt(choices.length)];
        answer = answer.trim();
        if (event.getSender() != null) {
            Utils.sendMessage(target, event.getSender() + ": " + answer);
        } else {
            Utils.sendMessage(target, answer);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "choose"
                };
    }
}
