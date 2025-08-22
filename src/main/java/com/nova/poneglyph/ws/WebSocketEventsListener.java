
package com.nova.poneglyph.ws;

//import com.nova.poneglyph.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

//@Component
//public class WebSocketEventsListener {
//
//    @EventListener
//    public void handleSessionConnectEvent(SessionConnectedEvent event) {
//        Principal user = event.getUser();
//        System.out.println("🔔 CONNECTED USER: " + (user != null ? user.getName() : "null"));
//    }
//}

@Component
@RequiredArgsConstructor
public class WebSocketEventsListener {
//    private final PresenceService presenceService;

    @EventListener
    public void handleSessionConnectEvent(SessionConnectedEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            String userId = user.getName();
//            presenceService.handleConnectionEvent(userId, true);
            System.out.println("🔔 CONNECTED USER: " + userId);
        }
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            String userId = user.getName();
//            presenceService.handleConnectionEvent(userId, false);
            System.out.println("🔔 DISCONNECTED USER: " + userId);
        }
    }
}
