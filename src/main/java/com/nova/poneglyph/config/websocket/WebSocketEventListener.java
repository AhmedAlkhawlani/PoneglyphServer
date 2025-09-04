//
//package com.nova.poneglyph.config.websocket;
//
//import com.nova.poneglyph.service.SessionService;
//import com.nova.poneglyph.service.presence.PresenceService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.event.EventListener;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.messaging.SessionConnectEvent;
//import org.springframework.web.socket.messaging.SessionDisconnectEvent;
//import org.springframework.web.socket.messaging.SessionSubscribeEvent;
//import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
//
//import java.security.Principal;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//
//@Component
//public class WebSocketEventListener {
//
//    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
//
//    private final PresenceService presenceService;
//    private final SessionService sessionService;
//
//    public WebSocketEventListener(PresenceService presenceService, SessionService sessionService) {
//        this.presenceService = presenceService;
//        this.sessionService = sessionService;
//    }
//
//
////@EventListener
////public void handleWebSocketConnectListener(SessionConnectEvent event) {
////    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
////    Principal principal = accessor.getUser();
////
////    if (principal != null) {
////        try {
////            UUID userId = UUID.fromString(principal.getName());
////            String sessionId = accessor.getSessionId();
////
////            log.info("WebSocket connected - User: {}, Session: {}", userId, sessionId);
////
////            // استخدام معالجة غير متزامنة لتسريع الاستجابة
////            CompletableFuture.runAsync(() -> {
////                sessionService.findLatestActiveSessionByUserId(userId).ifPresent(userSession -> {
////                    sessionService.setWebsocketSessionId(userSession.getId(), sessionId);
////                    presenceService.updateOnlineStatus(userId, userSession.getId(), true, sessionId);
////                    sessionService.updateSessionActivity(userSession.getId());
////                    presenceService.handleHeartbeat(userId, userSession.getId());
////                });
////            });
////
////        } catch (Exception e) {
////            log.warn("Failed to process WebSocket connection: {}", e.getMessage());
////        }
////    }
////}
//
//
//
//    @EventListener
//    public void handleWebSocketConnectListener(SessionConnectEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        Principal principal = accessor.getUser();
//
//        if (principal != null) {
//            try {
//                UUID userId = UUID.fromString(principal.getName());
//                String sessionId = accessor.getSessionId();
//
//                log.info("WebSocket connected - User: {}, Session: {}", userId, sessionId);
//
//                // البحث عن الجلسة مباشرة
//                sessionService.findLatestActiveSessionByUserId(userId).ifPresent(userSession -> {
//
//                    // --- تحديث مباشر فورياً على Redis والـ WebSocket ---
//                    presenceService.updateOnlineStatusImmediate(userId, userSession.getId(), true, sessionId);
//
//                    // --- العمليات الثقيلة في thread آخر ---
//                    CompletableFuture.runAsync(() -> {
//                        sessionService.setWebsocketSessionId(userSession.getId(), sessionId);
//                        sessionService.updateSessionActivity(userSession.getId());
//                        presenceService.handleHeartbeat(userId, userSession.getId());
//                    });
//                });
//
//            } catch (Exception e) {
//                log.warn("Failed to process WebSocket connection: {}", e.getMessage());
//            }
//        }
//    }
//
//    @EventListener
//    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        Principal principal = accessor.getUser();
//
//        if (principal != null) {
//            try {
//                UUID userId = UUID.fromString(principal.getName());
//                String sessionId = accessor.getSessionId();
//
//                log.info("WebSocket disconnected - User: {}, Session: {}", userId, sessionId);
//
//                // تحديث حالة الاتصال
//                sessionService.findByWebsocketSessionId(sessionId).ifPresent(userSession -> {
//                    sessionService.clearWebsocketSessionId(userSession.getId());
//                    presenceService.updateOnlineStatus(userId, userSession.getId(), false, null);
//                });
//
//            } catch (Exception e) {
//                log.warn("Failed to process WebSocket disconnection: {}", e.getMessage());
//            }
//        }
//    }
//
//    @EventListener
//    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        Principal principal = accessor.getUser();
//        String destination = accessor.getDestination();
//
//        if (principal != null && destination != null) {
//            try {
//                UUID userId = UUID.fromString(principal.getName());
//                log.debug("User {} subscribed to {}", userId, destination);
//
//                // تحديث النشاط عند الاشتراك
//                sessionService.findLatestActiveSessionByUserId(userId).ifPresent(session -> {
//                    sessionService.updateSessionActivity(session.getId());
//                    presenceService.handleHeartbeat(userId, session.getId());
//                });
//
//                // معالجة أنواع الاشتراكات المختلفة
//                if (destination.startsWith("/user/queue/call")) {
//                    log.info("User {} subscribed to call notifications", userId);
//                } else if (destination.startsWith("/topic/conversation.")) {
//                    String conversationId = destination.replace("/topic/conversation.", "");
//                    log.info("User {} subscribed to conversation {}", userId, conversationId);
//                } else if (destination.startsWith("/topic/presence.")) {
//                    String presenceUserId = destination.replace("/topic/presence.", "");
//                    log.info("User {} subscribed to presence of user {}", userId, presenceUserId);
//                }
//
//            } catch (Exception e) {
//                log.warn("Failed to process subscription: {}", e.getMessage());
//            }
//        }
//    }
//
//    @EventListener
//    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        Principal principal = accessor.getUser();
//        String destination = accessor.getDestination();
//
//        if (principal != null && destination != null) {
//            try {
//                UUID userId = UUID.fromString(principal.getName());
//                log.debug("User {} unsubscribed from {}", userId, destination);
//
//            } catch (Exception e) {
//                log.warn("Failed to process unsubscription: {}", e.getMessage());
//            }
//        }
//    }
//}
//
//package com.nova.poneglyph.config.websocket;
//
//import com.nova.poneglyph.service.SessionService;
//import com.nova.poneglyph.service.presence.PresenceService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.event.EventListener;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.messaging.SessionConnectEvent;
//import org.springframework.web.socket.messaging.SessionDisconnectEvent;
//import org.springframework.web.socket.messaging.SessionSubscribeEvent;
//
//import java.security.Principal;
//import java.util.UUID;
//
//@Component
//public class WebSocketEventListener {
//
//    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
//
//    private final PresenceService presenceService;
//    private final SessionService sessionService;
//
//    public WebSocketEventListener(PresenceService presenceService, SessionService sessionService) {
//        this.presenceService = presenceService;
//        this.sessionService = sessionService;
//    }
//
//    /**
//     * عند اتصال المستخدم — كل التحديثات فورية:
//     * - تحديث websocketSessionId في DB
//     * - تحديث lastActivity و online في DB
//     * - تحديث Redis فوراً
//     * - إرسال إشعار presence فوراً
//     */
//    @EventListener
//    public void handleWebSocketConnectListener(SessionConnectEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        Principal principal = accessor.getUser();
//
//        if (principal == null) return;
//
//        try {
//            UUID userId = UUID.fromString(principal.getName());
//            String websocketSessionId = accessor.getSessionId();
//
//            log.info("WebSocket connected - User: {}, WS Session: {}", userId, websocketSessionId);
//
//            sessionService.findLatestActiveSessionByUserId(userId).ifPresent(userSession -> {
//                UUID sessionId = userSession.getId();
//
//                // --- التحديثات الفورية (synchronous) ---
//                // ضع websocketSessionId وعلّم الجلسة أونلاين في DB
//                sessionService.setWebsocketSessionId(sessionId, websocketSessionId);
//                sessionService.updateSessionActivity(sessionId); // lastUsedAt و lastActivity
//
//                // حدث الحالة في Redis وأرسل الإشعار فوراً
//                presenceService.updateOnlineStatusImmediate(userId, sessionId, true, websocketSessionId);
//
//                // heartbeat صغير فوري (تجديد الوقت في Redis/DB)
//                presenceService.handleHeartbeatImmediate(userId, sessionId);
//            });
//
//        } catch (Exception e) {
//            log.warn("Failed to process WebSocket connection: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * عند قطع الاتصال — تحديث فوري كذلك.
//     */
//    @EventListener
//    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        Principal principal = accessor.getUser();
//        String websocketSessionId = accessor.getSessionId();
//
//        if (principal == null) return;
//
//        try {
//            UUID userId = UUID.fromString(principal.getName());
//            log.info("WebSocket disconnected - User: {}, WS Session: {}", userId, websocketSessionId);
//
//            // نبحث عن الجلسة بالـ websocketSessionId أو الجلسة الأخيرة
//            sessionService.findByWebsocketSessionId(websocketSessionId).ifPresentOrElse(userSession -> {
//                UUID sessionId = userSession.getId();
//
//                // تحديث فوري: حذف websocketSessionId، وضع offline في Redis والـ DB، إشعار فورياً
//                sessionService.clearWebsocketSessionId(sessionId);
//                presenceService.updateOnlineStatusImmediate(userId, sessionId, false, null);
//                presenceService.handleHeartbeatImmediate(userId, sessionId);
//            }, () -> {
//                // كملء احتياطي: إذا لم تُعثر الجلسة بواسطة websocketSessionId، حاول الجلسة الأخيرة للمستخدم
//                sessionService.findLatestActiveSessionByUserId(userId).ifPresent(userSession -> {
//                    UUID sessionId = userSession.getId();
//                    sessionService.clearWebsocketSessionId(sessionId);
//                    presenceService.updateOnlineStatusImmediate(userId, sessionId, false, null);
//                    presenceService.handleHeartbeatImmediate(userId, sessionId);
//                });
//            });
//
//        } catch (Exception e) {
//            log.warn("Failed to process WebSocket disconnection: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * عند الاشتراك: نجدد النشاط فوراً ونعالج heartbeat فورياً.
//     */
//    @EventListener
//    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        Principal principal = accessor.getUser();
//        String destination = accessor.getDestination();
//
//        if (principal == null) return;
//
//        try {
//            UUID userId = UUID.fromString(principal.getName());
//            sessionService.findLatestActiveSessionByUserId(userId).ifPresent(session -> {
//                sessionService.updateSessionActivity(session.getId());
//                presenceService.handleHeartbeatImmediate(userId, session.getId());
//            });
//            log.debug("User {} subscribed to {}", userId, destination);
//        } catch (Exception e) {
//            log.warn("Failed to process subscription: {}", e.getMessage(), e);
//        }
//    }
//}
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
        if (principal == null) return;
        try {
            UUID userId = UUID.fromString(principal.getName());
            String websocketSessionId = accessor.getSessionId();
            log.info("WebSocket connected - User: {}, WS Session: {}", userId, websocketSessionId);

            sessionService.findLatestActiveSessionByUserId(userId).ifPresent(userSession -> {
                UUID sessionId = userSession.getId();
                sessionService.setWebsocketSessionId(sessionId, websocketSessionId);
                sessionService.updateSessionActivity(sessionId);
                presenceService.updateOnlineStatusImmediate(userId, sessionId, true, websocketSessionId);
                presenceService.handleHeartbeatImmediate(userId, sessionId);
            });
        } catch (Exception e) {
            log.warn("Failed to process WebSocket connection: {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String websocketSessionId = accessor.getSessionId();
        if (principal == null) return;
        try {
            UUID userId = UUID.fromString(principal.getName());
            log.info("WebSocket disconnected - User: {}, WS Session: {}", userId, websocketSessionId);

            sessionService.findByWebsocketSessionId(websocketSessionId).ifPresentOrElse(userSession -> {
                UUID sessionId = userSession.getId();
                sessionService.clearWebsocketSessionId(sessionId);
                presenceService.updateOnlineStatusImmediate(userId, sessionId, false, null);
                presenceService.handleHeartbeatImmediate(userId, sessionId);
            }, () -> {
                sessionService.findLatestActiveSessionByUserId(userId).ifPresent(userSession -> {
                    UUID sessionId = userSession.getId();
                    sessionService.clearWebsocketSessionId(sessionId);
                    presenceService.updateOnlineStatusImmediate(userId, sessionId, false, null);
                    presenceService.handleHeartbeatImmediate(userId, sessionId);
                });
            });
        } catch (Exception e) {
            log.warn("Failed to process WebSocket disconnection: {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String destination = accessor.getDestination();
        if (principal == null) return;
        try {
            UUID userId = UUID.fromString(principal.getName());
            sessionService.findLatestActiveSessionByUserId(userId).ifPresent(session -> {
                sessionService.updateSessionActivity(session.getId());
                presenceService.handleHeartbeatImmediate(userId, session.getId());
            });
            log.debug("User {} subscribed to {}", userId, destination);
        } catch (Exception e) {
            log.warn("Failed to process subscription: {}", e.getMessage(), e);
        }
    }
}
