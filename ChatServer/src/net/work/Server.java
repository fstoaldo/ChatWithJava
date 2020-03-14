package net.work;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {

    private final int serverPort;
    private ArrayList<ServerThread> threadList = new ArrayList<>();

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    public List<ServerThread> getServerThread() {
        return threadList;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            // while loop to accept multiple clients/threads
            while (true) {
                System.out.print("Accepting client connection...\n");
                // accept method accepts the connection w/ client and returns a new client socket
                Socket clientSocket = serverSocket.accept();
                System.out.println("OK");
                System.out.println("Connected to: " + clientSocket);
                // creates a new thread for each client attempting to connect
                ServerThread serverThread = new ServerThread(this, clientSocket);
                threadList.add(serverThread);
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeThread(ServerThread serverThread) {
        // remove users that logged off from threadList
        threadList.remove(serverThread);
    }
}
