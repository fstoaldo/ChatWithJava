package net.work;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

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
    private BufferedReader bufferedIn;

    // allow multiple user listeners (detect if other users are online or offline)
    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    // allow multiple message listeners
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public Client(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("YOU GOT A MESSAGE FROM " + fromLogin + " --> " + msgBody);
            }
        });

        // connects to the server
        if (!client.connect()) {
            System.err.println("CONNECTION FAILED");
        }
        else {
            System.out.println("CONNECTION ESTABLISHED");
            if (client.login("guest", "guest")) {
                System.out.println("LOGIN SUCCESSFUL");
                client.msg("jim", "HELLO"); // testing
            }
            else {
                System.err.println("LOGIN FAILED");
            }

            // client.logoff();
        }
    }

    private void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    private boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        // read line from the server
        String response = bufferedIn.readLine();
        System.out.println("Response line: " + response);

        if ("Login OK".equalsIgnoreCase(response)) {
            // start listening to server responses
            startMessageReader();
            return true;
        }
        else {
            return false;
        }
    }

    private void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                // split line into tokens
                String[] tokens = StringUtils.split(line);
                // check tokens for null pointers
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)){
                        handleOnline(tokens);
                    }
                    else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    }
                    else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];

        for (MessageListener listener : messageListeners) {
            listener.onMessage(login, msgBody);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.online(login);
        }
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
            this.bufferedIn = new BufferedReader((new InputStreamReader(serverIn)));
            Thread inputListener = new Thread(new ResponseListener(socket));
            inputListener.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // methods so other components can register / remove a listener to chat client
    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
}

