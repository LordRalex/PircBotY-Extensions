package com.lordralex.bouncer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Bouncer {

    public static void main(String[] args) throws IOException {
        ServerSocket server;
        server = new ServerSocket(25565);
        Socket mainClient;
        Socket output = new Socket("192.168.1.1", 25565);

        while(true)
        {
            mainClient = server.accept();
            System.out.println("Connection established");
            InputStream reader = mainClient.getInputStream();
            int line;
            while((line = reader.read()) != -1)
            {
                output.getOutputStream().write(line);
            }
            System.out.println("Connection closed");
        }
    }
}
