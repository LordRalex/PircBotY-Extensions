
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.settings.Settings;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import jline.console.ConsoleReader;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class SayTerminalListener extends Listener implements Runnable {

    ServerSocket server;

    @Override
    public void setup() {
        int port = Settings.getGlobalSettings().getInt("say-port");
        if (port > 0) {
            try {
                server = new ServerSocket(port);
            } catch (IOException ex) {
                Logger.getLogger(SayTerminalListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void run() {
        boolean run = true;
        while (run) {
            try {
                Socket client = server.accept();
                Client cl = new Client(client);
                cl.start();
            } catch (IOException ex) {
                Logger.getLogger(SayTerminalListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class Client extends Thread {

        Socket socket;
        boolean isAllowed = false;
        BufferedReader input;
        BufferedWriter output;
        String chan;

        public Client(Socket s) throws IOException {
            Logger.getLogger(SayTerminalListener.class.getName()).info("Connection made from " + s.getInetAddress());
            socket = s;
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        @Override
        public void run() {
            int tries = 0;
            while (socket.isConnected()) {
                if (!isAllowed) {
                    try {
                        output.write("Send verification code");
                        TimeoutThread timeout = new TimeoutThread(this, 30);
                        timeout.start();
                        String pass = input.readLine();
                        if (!pass.equals(Settings.getGlobalSettings().getString("socket-pass"))) {
                            output.write("Invalid pass");
                            tries++;
                            if (tries >= 3) {
                                socket.close();
                            }
                        } else {
                            output.write("Connection good, you can talk");
                            isAllowed = true;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(SayTerminalListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        String line = input.readLine();
                        if (line.startsWith("$")) {
                            line = line.substring(1);
                            if (line.startsWith("channel")) {
                                chan = line.split(" ")[1];
                            } else if (line.startsWith("close")) {
                                output.write("Closing");
                                socket.close();
                            }
                        } else {
                            if (chan == null || chan.isEmpty()) {
                                output.write("No channel selected");
                            } else {
                                Utils.sendMessage(chan, line);
                            }
                        }
                    } catch (IOException e) {
                        Logger.getLogger(SayTerminalListener.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
        }
    }

    private class TimeoutThread extends Thread {

        Thread parent;
        int seconds;

        public TimeoutThread(Thread p, int timeOut) {
            parent = p;
            seconds = timeOut * 1000;
        }

        @Override
        public void run() {
            synchronized (this) {
                try {
                    this.wait(seconds);
                    parent.interrupt();
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
