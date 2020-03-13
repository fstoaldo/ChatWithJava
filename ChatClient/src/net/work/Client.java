package net.work;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Flavio
 * March 13 2020
 */

public class Client {
    private final String serverName;
    private final int serverPort;
    private OutputStream serverOut;
    private InputStream serverIn;
    private Socket socket;

    public Client(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 8818);
        // connects to the server
        if (!client.connect()) {
            System.err.println("CONNECTION FAILED");
        }
        else {
            System.out.println("CONNECTION ESTABLISHED");
            client.login("guest", "guest");
        }
    }

    private void login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());
    }

    private boolean connect() {
        // new socket to establish connection with the server
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port: " + socket.getLocalPort());
            // client --> server
            this.serverOut = socket.getOutputStream();
            // server --> client
            this.serverIn = socket.getInputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

