package net.work;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Flavio
 * March 05 2020
 */

public class ServerMain {
    public static void main(String[] args) {
        // in order to have a network connection, a server socket is needed
        // socket = ip + port
        int port = 8818;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            // while loop to accept multiple clients/threads
            while (true) {
                System.out.print("Accepting client connection...");
                // accept method accepts the connection w/ client and returns a new client socket
                Socket clientSocket = serverSocket.accept();
                System.out.println("OK");
                System.out.println("Connected to: " + clientSocket);
                // creates a new thread for each client attempting to connect
                ServerThread serverThread = new ServerThread(clientSocket);
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
