
package com.nova.poneglyph.ws;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.security.Principal;

@Component
public class WebSocketEventsListener {

    @EventListener
    public void handleSessionConnectEvent(SessionConnectedEvent event) {
        Principal user = event.getUser();
        System.out.println("🔔 CONNECTED USER: " + (user != null ? user.getName() : "null"));
    }
}
