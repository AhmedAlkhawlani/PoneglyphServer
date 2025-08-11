package com.nova.poneglyph.config;

public interface PresenceNotifier {
    void handleConnectionEvent(String phoneNumber, boolean isConnected);
    void updateLastActivity(String phoneNumber);

    void handleHeartbeat(String name);

}
