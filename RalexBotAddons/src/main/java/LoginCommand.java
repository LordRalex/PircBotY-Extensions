
import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.settings.Settings;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.security.cert.Certificate;
import javax.net.ssl.HttpsURLConnection;
import org.pircbotx.Colors;

public class LoginCommand extends Listener {

    private boolean useCache = false;
    private boolean lastTest = true;
    private Settings settings;

    @Override
    public void setup() {
        settings = new Settings(new File("settings", "config.yml"));
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        final String channel = event.getChannel();
        final String sender = event.getSender();
        if (!useCache) {
            String userName = settings.getString("mcnick");
            String password = settings.getString("mcpass");
            try {
                String parameters;
                parameters = "user=" + URLEncoder.encode(userName, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8") + "&version=" + 13;

                String result = testConnection("https://login.minecraft.net/", parameters);
                if (result == null) {
                    lastTest = false;
                } else {
                    lastTest = true;
                }
            } catch (UnsupportedEncodingException ex) {
            }
        }
        if (!lastTest) {
            if (channel != null) {
                Utils.sendMessage(channel, "Login Server Status: " + Colors.RED + Colors.BOLD + "Offline");
            } else if (sender != null) {
                Utils.sendMessage(sender, "Login Server Status: " + Colors.RED + Colors.BOLD + "Offline");
            }
        } else {
            if (channel != null) {
                Utils.sendMessage(channel, "Login Server Status: " + Colors.GREEN + Colors.BOLD + "Online");
            } else if (sender != null) {
                Utils.sendMessage(sender, "Login Server Status: " + Colors.GREEN + Colors.BOLD + "Online");
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "login"
                };
    }

    private static String testConnection(String targetURL, String urlParameters) {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(targetURL);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(5000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            Certificate[] certs = connection.getServerCertificates();
            byte[] bytes = new byte[294];
            DataInputStream dis = new DataInputStream(RalexBot.class.getResourceAsStream("/resources/minecraft.key"));
            dis.readFully(bytes);
            Certificate c = certs[0];
            PublicKey pk = c.getPublicKey();
            byte[] data = pk.getEncoded();
            for (int i = 0; i < data.length; i++) {
                if (data[i] == bytes[i]) {
                    continue;
                }
                throw new IOException("Public key mismatch");
            }
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }
            InputStream is = connection.getInputStream();
            StringBuilder response;
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
                response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
            }
            String str1 = response.toString();
            return str1;
        } catch (IOException e) {
            if (connection != null) {
                connection.disconnect();
            }
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
