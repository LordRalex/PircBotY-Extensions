
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class PingServerCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final String channel = event.getChannel();
        final String sender = event.getSender();
        final String[] args = event.getArgs();
        String target = channel;
        if (channel == null && sender == null) {
            return;
        }
        if (channel == null) {
            target = sender;
        }
        final String dest = target;
        String ip = args[0];
        String port;
        if (args.length >= 2) {
            port = args[1];
        } else {
            port = "25565";
            if (ip.contains(":")) {
                port = ip.split(":")[1];
                ip = ip.split(":")[0];
            }
        }
        int portA = 25565;
        try {
            portA = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            if (port.startsWith("[") && port.endsWith("]")) {
                port = port.substring(1, port.length() - 2);
                try {
                    portA = Integer.parseInt(port);
                } catch (NumberFormatException ex) {
                    Utils.sendMessage(dest, "I could not convert " + port + " to a number, make sure it is only digits.");
                    return;
                }
            }
        }
        try {
            InetAddress.getByName(ip);
        } catch (UnknownHostException ex) {
            Utils.sendMessage(dest, "The IP you entered could not be tested");
            return;
        }
        Object[] results = test(ip, portA);
        if (results[0] == Boolean.TRUE) {
            Utils.sendMessage(dest, "IP: " + ip + ":" + portA + " was reachable. Players: " + results[2] + "/" + results[3] + " MOTD: " + results[1]);
        } else {
            Utils.sendMessage(dest, "I could not connect to " + ip + ":" + portA);
        }

    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "pingserver"
                };
    }

    public Object[] test(String ip, int port) {

        Socket pingTest = null;
        DataInputStream localDataInputStream = null;
        DataOutputStream localDataOutputStream = null;
        Object[] results = new Object[4];
        try {
            pingTest = new Socket();
            pingTest.setSoTimeout(5000);
            pingTest.setTcpNoDelay(true);
            pingTest.setTrafficClass(18);
            pingTest.connect(new InetSocketAddress(ip, port), 3000);
            localDataInputStream = new DataInputStream(pingTest.getInputStream());
            localDataOutputStream = new DataOutputStream(pingTest.getOutputStream());
            localDataOutputStream.write(254);
            if (localDataInputStream.read() != 255) {
                throw new IOException("Bad message");
            }
            String str4 = a(localDataInputStream, 256);
            char[] arrayOfChar = str4.toCharArray();
            String font = b();
            for (int i3 = 0; i3 < arrayOfChar.length; i3++) {
                if ((arrayOfChar[i3] != '§') && (font.indexOf(arrayOfChar[i3]) < 0)) {
                    arrayOfChar[i3] = '?';
                }
            }
            str4 = new String(arrayOfChar);
            String[] arrayOfString = str4.split("§");
            str4 = arrayOfString[0];
            int i3 = Integer.parseInt(arrayOfString[1]);
            int i4 = Integer.parseInt(arrayOfString[2]);
            results[0] = Boolean.TRUE;
            results[1] = str4;
            results[2] = i3;
            results[3] = i4;
        } catch (IOException ex) {
            results[0] = Boolean.FALSE;
            results[1] = null;
            results[2] = null;
            results[3] = null;
        } finally {
            try {
                if (localDataInputStream != null) {
                    localDataInputStream.close();
                }
            } catch (Throwable a) {
            }
            try {
                if (localDataOutputStream != null) {
                    localDataOutputStream.close();
                }
            } catch (Throwable b) {
            }
            try {
                if (pingTest != null) {
                    pingTest.close();
                }
            } catch (Throwable c) {
            }
            return results;
        }
    }

    private String a(DataInputStream paramDataInputStream, int paramInt) throws IOException {
        int i = paramDataInputStream.readShort();
        if (i > paramInt) {
            throw new IOException("Received string length longer than maximum allowed (" + i + " > " + paramInt + ")");
        }
        if (i < 0) {
            throw new IOException("Received string length is less than zero! Weird string!");
        }
        StringBuilder localStringBuilder = new StringBuilder();
        for (int j = 0; j < i; j++) {
            localStringBuilder.append(paramDataInputStream.readChar());
        }
        return localStringBuilder.toString();
    }

    private String b() {
        String str1 = "";
        try {
            BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(PingServerCommand.class.getResourceAsStream("/resources/font.txt"), "UTF-8"));
            String str2;
            while ((str2 = localBufferedReader.readLine()) != null) {
                if (!str2.startsWith("#")) {
                    str1 = str1 + str2;
                }
            }

        } catch (Exception a) {
        }
        return str1;
    }
}
