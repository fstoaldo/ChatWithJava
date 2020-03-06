package net.work;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {

    private final Socket clientSocket;

    public ServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    // every thread has a run method
    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();

        // to read line by line
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            if ("quit".equalsIgnoreCase(inputLine)) {
                break;
            }
            // send message to outputStream
            String msg = "ECHO: " + inputLine + "\n";
            outputStream.write(msg.getBytes());
        }
        clientSocket.close();
    }
}
