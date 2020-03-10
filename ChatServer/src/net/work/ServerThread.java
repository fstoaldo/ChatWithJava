package net.work;

import java.io.*;
import java.net.Socket;
import org.apache.commons.lang3.StringUtils;

public class ServerThread extends Thread {

    private final Socket clientSocket;
    private String login = null;

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

        outputStream.write("Connection established\n".getBytes());

        // to read line by line
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            // split line into tokens
            String[] tokens = StringUtils.split(inputLine);
            // check tokens for null pointers
            if (tokens != null && tokens.length > 0) {
                // command will be the first token
                String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(cmd)) {
                    break;
                }
                // handle user logins
                else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                }
                else {
                    // send message to outputStream
                    String msg = "!UNKNOWN: <" + cmd + ">\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            // login <username> <passwd>
            String login = tokens[1];
            String passwd = tokens[2];

            // allow for guest logins
            if (login.equals("guest") && passwd.equals("guest")) {
                String msg = "Login OK\n";
                outputStream.write(msg.getBytes());
                // tie this thread to the login
                this.login = login;
                System.out.println("User logged in: " + login);
            } else {
                String msg = "login ERROR\n";
                outputStream.write(msg.getBytes());
            }
        }
    }
}
