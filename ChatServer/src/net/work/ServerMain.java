package net.work;

/**
 * Flavio
 * March 05 2020
 */

public class ServerMain {
    public static void main(String[] args) {
        // in order to have a network connection, a server socket is needed
        // socket = ip + port
        int port = 8818;
        Server server = new Server(port);
        server.start();
    }
}
