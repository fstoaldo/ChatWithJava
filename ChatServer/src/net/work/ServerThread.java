package net.work;

import java.io.*;
import java.net.Socket;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ServerThread extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;

    public ServerThread(Server server, Socket clientSocket) {
        this.server = server;
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
        this.outputStream = clientSocket.getOutputStream();

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
                if ("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
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

    private void handleLogoff() throws IOException {
        // remove this thread from thread list
        server.removeThread(this);
        // send to other online users current users's status
        List<ServerThread> threadList = server.getServerThread();
        String offMsg = "OFFLINE: " + login + "\n";
        for (ServerThread thread : threadList) {
            if (!login.equals(thread.getLogin())) {
                thread.send(offMsg);
            }
        }
        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            // login <username> <passwd>
            String login = tokens[1];
            String passwd = tokens[2];

            // allow for guest logins
            if ((login.equals("guest") && passwd.equals("guest")) || (login.equals("jim") && passwd.equals("jim"))) {
                String msg = "Login OK\n";
                outputStream.write(msg.getBytes());
                // tie this thread to the login
                this.login = login;
                System.out.println("User logged in: " + login);

                // send to current user all other online logins
                List<ServerThread> threadList = server.getServerThread();
                for (ServerThread thread : threadList) {
                    // avoid sending own login to output
                    if (!login.equals(thread.getLogin())) {
                        if (thread.getLogin() != null) {
                            String msg2 = "ONLINE: " + thread.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }
                // send to other online users current users's status
                String onMsg = "ONLINE: " + login + "\n";
                for (ServerThread thread : threadList) {
                    if (!login.equals(thread.getLogin())) {
                        thread.send(onMsg);
                    }
                }
                
            } else {
                String msg = "login ERROR\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void send(String msg) throws IOException {
        // avoid sending null to output
        if (login != null) {
            outputStream.write(msg.getBytes());
        }

    }
}
