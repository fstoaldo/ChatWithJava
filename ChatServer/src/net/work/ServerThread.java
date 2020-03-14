package net.work;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ServerThread extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<String>();

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

        // outputStream.write("Connection established\n".getBytes());

        // to read line by line
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        // FIXME (connection reset, client login)
        while ((inputLine = reader.readLine()) != null) {
            // split line into tokens
            String[] tokens = StringUtils.split(inputLine);
            // check tokens for null pointers
            if (tokens != null && tokens.length > 0) {
                // command will be the first token
                String cmd = tokens[0];
                // handle user logoff
                if ("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                }
                // handle user logins
                else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                }
                // handle direct messages
                else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(inputLine,null, 3);
                    handleMessage(tokensMsg);
                }
                // handles user joining a topic
                else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                }
                // handles user leaving a topic
                else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);
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

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    public boolean isMemberOfTopic (String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    // format for direct messaging: msg login "text..."
    // format for group messaging: #topic "text..."
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String txtBody = tokens[2];

        // check if sending to topic or user
        boolean isTopic = sendTo.charAt(0) == '#';

        List<ServerThread> threadList = server.getServerThread();
        for (ServerThread thread : threadList) {
            if (isTopic) {
                if (thread.isMemberOfTopic(sendTo)) {
                    String outMsg = "msg " + sendTo + ": " + login + " " + txtBody + "\n";
                    thread.send(outMsg);
                }
            }
            else {
                if (sendTo.equalsIgnoreCase(thread.getLogin())) {
                    String outMsg = "msg " + login + " " + txtBody + "\n";
                    thread.send(outMsg);
                }
            }
        }
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

            // allow logins
            if ((login.equals("guest") && passwd.equals("guest")) || (login.equals("jim") && passwd.equals("jim"))) {
                String msg = "login ok";
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
                System.err.println("LOGIN FAILED FOR: " + login);
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
