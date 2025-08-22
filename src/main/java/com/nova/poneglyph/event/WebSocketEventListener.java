package com.nova.poneglyph.event;



import com.nova.poneglyph.domain.user.User;

import com.nova.poneglyph.service.presence.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal != null) {
            UUID userId = UUID.fromString(principal.getName());
            String sessionId = accessor.getSessionId();
//            presenceService.setOnline(userId, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal != null) {
            UUID userId = UUID.fromString(principal.getName());
//            presenceService.setOffline(userId);
        }
    }
}
