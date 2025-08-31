//package com.nova.poneglyph.config.websocket;
//
//import com.nova.poneglyph.domain.user.UserSession;
//import com.nova.poneglyph.service.SessionService;
//import com.nova.poneglyph.service.presence.PresenceService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.OffsetDateTime;
//import java.util.List;
//
//@Service
//public class WebSocketMaintenanceService {
//
//    private static final Logger log = LoggerFactory.getLogger(WebSocketMaintenanceService.class);
//
//    private final SessionService sessionService;
//    private final PresenceService presenceService;
//
//    public WebSocketMaintenanceService(SessionService sessionService, PresenceService presenceService) {
//        this.sessionService = sessionService;
//        this.presenceService = presenceService;
//    }
//
//    @Scheduled(fixedRate = 300000)
//    @Transactional
//    public void cleanupInactiveSessions() {
//        try {
//            // الاعتماد على Redis كالمصدر الرئيسي للحالة
//            List<UserSession> allSessions = sessionService.getAllActiveSessions();
//
//            for (UserSession session : allSessions) {
//                boolean isActiveInRedis = presenceService.isUserOnline(session.getUser().getId());
//                boolean isExpired = session.getLastActivity().isBefore(OffsetDateTime.now().minusMinutes(5));
//
//                if (!isActiveInRedis && isExpired) {
//                    log.debug("Cleaning up inactive session: {}", session.getId());
//                    sessionService.clearWebsocketSessionId(session.getId());
//                    presenceService.updateOnlineStatus(
//                            session.getUser().getId(),
//                            session.getId(),
//                            false,
//                            null
//                    );
//                }
//            }
//        } catch (Exception e) {
//            log.error("Failed to cleanup inactive sessions: {}", e.getMessage());
//        }
//    }
//
//    // إرسال دقات القلب للاتصالات النشطة كل دقيقة
//    @Scheduled(fixedRate = 60000)
//    public void sendHeartbeats() {
//        try {
//            sessionService.getAllActiveSessions().forEach(session -> {
//                if (session.isOnline() && session.getWebsocketSessionId() != null) {
//                    // يمكنك إرسال رسالة heartbeat هنا إذا لزم الأمر
//                    log.debug("Sending heartbeat to session: {}", session.getId());
//
//                    // مثال: إرسال رسالة heartbeat عبر WebSocket
////                     webSocketService.sendHeartbeat(session.getWebsocketSessionId());
//                }
//            });
//        } catch (Exception e) {
//            log.error("Failed to send heartbeats: {}", e.getMessage());
//        }
//    }
//}

package com.nova.poneglyph.config.websocket;

import com.nova.poneglyph.domain.user.UserSession;
import com.nova.poneglyph.service.SessionService;
import com.nova.poneglyph.service.presence.PresenceService;
import com.nova.poneglyph.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class WebSocketMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMaintenanceService.class);

    private final SessionService sessionService;
    private final PresenceService presenceService;
    private final WebSocketService webSocketService;

    public WebSocketMaintenanceService(SessionService sessionService,
                                       PresenceService presenceService,
                                       WebSocketService webSocketService) {
        this.sessionService = sessionService;
        this.presenceService = presenceService;
        this.webSocketService = webSocketService;
    }

    @Scheduled(fixedRate = 300000) // 5 دقائق
    @Transactional
    public void cleanupInactiveSessions() {
        try {
            OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(5);

            sessionService.getAllActiveSessions().forEach(session -> {
                boolean isActiveInRedis = presenceService.isUserOnline(session.getUser().getId());
                boolean isExpired = session.getLastActivity().isBefore(threshold);

                // فقط الجلسات المنتهية وغير النشطة في Redis يتم تنظيفها
                if (!isActiveInRedis && isExpired) {
                    log.info("Cleaning up inactive session: {} for user: {}",
                            session.getId(), session.getUser().getId());

                    sessionService.clearWebsocketSessionId(session.getId());
                    presenceService.updateOnlineStatus(
                            session.getUser().getId(),
                            session.getId(),
                            false,
                            null
                    );
                } else if (isActiveInRedis && isExpired) {
                    // حالة وسيطة: نشط في Redis لكن منتهي في DB
                    // نجدد النشاط في قاعدة البيانات
                    session.setLastActivity(OffsetDateTime.now());
                    sessionService.saveSession(session);
                    log.debug("Refreshed expired but active session: {}", session.getId());
                }
            });
        } catch (Exception e) {
            log.error("Failed to cleanup inactive sessions: {}", e.getMessage());
        }
    }

    // إرسال دقات القلب للاتصالات النشطة كل دقيقة
    @Scheduled(fixedRate = 60000)
    public void sendHeartbeats() {
        try {
            sessionService.getAllActiveSessions().forEach(session -> {
                if (session.isOnline() && session.getWebsocketSessionId() != null) {
                    log.debug("Sending heartbeat to session: {}", session.getId());

                    // إرسال رسالة heartbeat فعلية
                    try {
                        webSocketService.sendHeartbeat(session.getWebsocketSessionId(), session.getUser().getId());
                        presenceService.handleHeartbeat(session.getUser().getId(), session.getId());
                    } catch (Exception e) {
                        log.warn("Failed to send heartbeat to session {}: {}", session.getId(), e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            log.error("Failed to send heartbeats: {}", e.getMessage());
        }
    }
}
