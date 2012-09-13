/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package networkexample;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Joshua
 */
public class NetworkExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ServerSocket server;

        //list of all clients connected
        List<Socket> clients = new ArrayList<Socket>();
        try {
            //binds to port 8181
            server = new ServerSocket(8181);

            if (!server.isBound() || server.isClosed()) {
                throw new RuntimeException("Error keeping port");
            }

            //keeps server alive
            boolean dead = false;
            while (!dead) {
                Socket client = server.accept();
                System.out.println("Connection made from " + client.toString());
                clients.add(client);
            }
        } catch (IOException ex) {
            Logger.getLogger(NetworkExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class Client extends Thread {

        Socket connection;

        public Client(Socket conn) {
            connection = conn;
        }

        @Override
        public void run() {
            while (connection != null && connection.isConnected() && !connection.isClosed()) {
                try {
                    int something = connection.getInputStream().read();
                    connection.getOutputStream().write(something);
                } catch (IOException e) {
                }
            }
        }
    }
}
