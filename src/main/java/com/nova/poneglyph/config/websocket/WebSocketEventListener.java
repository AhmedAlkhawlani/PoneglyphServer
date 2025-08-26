package com.nova.poneglyph.config.websocket;

import com.nova.poneglyph.service.SessionService;
import com.nova.poneglyph.service.presence.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.UUID;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final PresenceService presenceService;
    private final SessionService sessionService;

    public WebSocketEventListener(PresenceService presenceService, SessionService sessionService) {
        this.presenceService = presenceService;
        this.sessionService = sessionService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if (principal != null) {
            try {
                UUID userId = UUID.fromString(principal.getName());
                String sessionId = accessor.getSessionId();

                log.info("WebSocket connected - User: {}, Session: {}", userId, sessionId);

                // تحديث حالة الاتصال
                sessionService.findLatestActiveSessionByUserId(userId).ifPresent(userSession -> {
                    sessionService.setWebsocketSessionId(userSession.getId(), sessionId);
                    presenceService.updateOnlineStatus(userId, userSession.getId(), true, sessionId);
                });

            } catch (Exception e) {
                log.warn("Failed to process WebSocket connection: {}", e.getMessage());
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if (principal != null) {
            try {
                UUID userId = UUID.fromString(principal.getName());
                String sessionId = accessor.getSessionId();

                log.info("WebSocket disconnected - User: {}, Session: {}", userId, sessionId);

                // تحديث حالة الاتصال
                sessionService.findByWebsocketSessionId(sessionId).ifPresent(userSession -> {
                    sessionService.clearWebsocketSessionId(userSession.getId());
                    presenceService.updateOnlineStatus(userId, userSession.getId(), false, null);
                });

            } catch (Exception e) {
                log.warn("Failed to process WebSocket disconnection: {}", e.getMessage());
            }
        }
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String destination = accessor.getDestination();

        if (principal != null && destination != null) {
            try {
                UUID userId = UUID.fromString(principal.getName());
                log.debug("User {} subscribed to {}", userId, destination);

                // معالجة أنواع الاشتراكات المختلفة
                if (destination.startsWith("/user/queue/call")) {
                    log.info("User {} subscribed to call notifications", userId);
                } else if (destination.startsWith("/topic/conversation.")) {
                    String conversationId = destination.replace("/topic/conversation.", "");
                    log.info("User {} subscribed to conversation {}", userId, conversationId);
                } else if (destination.startsWith("/topic/presence.")) {
                    String presenceUserId = destination.replace("/topic/presence.", "");
                    log.info("User {} subscribed to presence of user {}", userId, presenceUserId);
                }

            } catch (Exception e) {
                log.warn("Failed to process subscription: {}", e.getMessage());
            }
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String destination = accessor.getDestination();

        if (principal != null && destination != null) {
            try {
                UUID userId = UUID.fromString(principal.getName());
                log.debug("User {} unsubscribed from {}", userId, destination);

            } catch (Exception e) {
                log.warn("Failed to process unsubscription: {}", e.getMessage());
            }
        }
    }
}
