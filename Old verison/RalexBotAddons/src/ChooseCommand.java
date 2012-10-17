
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import java.util.Random;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Joshua
 */
public class ChooseCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        String target = event.getChannel();
        if (target == null) {
            target = event.getSender();
        }
        if (target == null) {
            return;
        }

        String[] args = event.getArgs();
        if (args.length == 0) {
            sendMessage(target, "Command use: *choose [choices] (you can use spaces or , to separate them)");
            return;
        }

        String total = buildArgs(args);

        String[] choices;
        if (total.contains(",")) {
            choices = total.split(",");
        } else {
            choices = total.split(" ");
        }
        if (choices.length <= 1) {
            sendMessage(target, "What kind of a choice is that?");
            return;
        }

        String answer = choices[new Random().nextInt(choices.length)];
        answer = answer.trim();
        if (event.getSender() != null) {
            sendMessage(target, event.getSender() + ": " + answer);
        } else {
            sendMessage(target, answer);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "choose"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
