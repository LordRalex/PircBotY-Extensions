
import com.lordralex.ralexbot.RalexBotMain;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import com.lordralex.ralexbot.file.FileSystem;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import org.jibble.pircbot.Colors;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class LoginCommand extends Listener {

    boolean useCache = false;
    boolean lastTest = true;
    //Map<String, String> ignoreList = new HashMap<String, String>();

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final String channel = event.getChannel();
        final String sender = event.getSender();

        new Thread() {

            @Override
            public void run() {
                if (!useCache) {
                    String userName = FileSystem.getString("mcnick");
                    String password = FileSystem.getString("mcpass");
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
                        Logger.getLogger(LoginCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (!lastTest) {
                    if (channel != null) {
                        sendMessage(channel, "Login Server Status: " + Colors.RED + Colors.BOLD + "Offline");
                    } else if (sender != null) {
                        sendMessage(sender, "Login Server Status: " + Colors.RED + Colors.BOLD + "Offline");
                    } else {
                        RalexBotMain.print("Status: Offline");
                    }
                } else {
                    if (channel != null) {
                        sendMessage(channel, "Login Server Status: " + Colors.GREEN + Colors.BOLD + "Online");
                    } else if (sender != null) {
                        sendMessage(sender, "Login Server Status: " + Colors.GREEN + Colors.BOLD + "Online");
                    } else {
                        RalexBotMain.print("Status: Online");
                    }
                }
            }
        }.start();

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
            DataInputStream dis = new DataInputStream(RalexBotMain.class.getResourceAsStream("/minecraft.key"));
            dis.readFully(bytes);
            dis.close();
            Certificate c = certs[0];
            PublicKey pk = c.getPublicKey();
            byte[] data = pk.getEncoded();
            for (int i = 0; i < data.length; i++) {
                if (data[i] == bytes[i]) {
                    continue;
                }
                throw new IOException("Public key mismatch");
            }
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
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

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
