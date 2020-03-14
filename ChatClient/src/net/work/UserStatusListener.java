package net.work;

public interface UserStatusListener {
    // detects whether user is online or offline
    public void online(String login);
    public void offline(String login);
}
