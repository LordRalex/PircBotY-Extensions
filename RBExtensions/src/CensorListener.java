
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.settings.Settings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Joshua
 */
public class CensorListener extends Listener {

    List<String> censor = new ArrayList<String>();

    @Override
    public void setup() {
        List<String> list = Settings.getStringList("censor");
        if (list != null && !list.isEmpty()) {
            for (String word : list) {
                censor.add(word);
            }
        }
    }
}
