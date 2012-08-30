
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Joshua
 */
public class WeatherCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        BufferedReader reader = null;
        String total = this.buildArgs(event.getArgs());
        String target = event.getChannel();
        if (target == null) {
            target = event.getSender();
        }
        if (target == null) {
            return;
        }

        try {
            String url = "http://weather.com/weather/right-now/" + total.replace(" ", "%20");
            URL path = new URL(url);
            reader = new BufferedReader(new InputStreamReader(path.openStream()));
            List<String> parts = new ArrayList<>();
            String s;
            while ((s = reader.readLine()) != null) {
                parts.add(s);
            }
            List<String> b = new ArrayList<>();
            for (String part : parts) {
                String[] c = part.split(",");
                b.addAll(Arrays.asList(c));
            }
            boolean sent = false;
            for (String string : b) {
                if (string.startsWith("TWC.pco")) {

                    int humidity = Integer.parseInt(string.split("\"relativehumidity\":")[1].substring(0, 2).replace(",", "").replace("\"", ""));
                    int wind = Integer.parseInt(string.split("\"windspeed\":")[1].substring(0, 2).replace(",", "").replace("\"", ""));
                    int temp = Integer.parseInt(string.split("\"realtemp\":")[1].substring(0, 3).replace(",", "").replace("\"", ""));
                    int zip = Integer.parseInt(string.split("\"zip\":")[1].substring(0, 7).replace("\"", ""));

                    String answer = "Weather for " + zip + "-> Temp: " + temp + "F, Wind:  " + wind + "Humidity: " + humidity + "%";
                    sendMessage(target, total + ": " + answer);
                    sent = true;
                    break;
                }
            }
            if (!sent) {
                sendMessage(target, total + ": Nothing found for that location ;-;");
            }
        } catch (IOException ex) {
            Logger.getLogger(MCFCommand.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MCFCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "weather",
                    "we"
                };
    }

    @Override
    public void declarePriorities() {
        //priorities.put(EventType.Command, Priority.NORMAL);
    }
}
