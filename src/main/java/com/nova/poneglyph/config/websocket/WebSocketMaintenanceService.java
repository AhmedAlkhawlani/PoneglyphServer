package com.nova.poneglyph.config.websocket;

import com.nova.poneglyph.service.SessionService;
import com.nova.poneglyph.service.presence.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class WebSocketMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMaintenanceService.class);

    private final SessionService sessionService;
    private final PresenceService presenceService;

    public WebSocketMaintenanceService(SessionService sessionService, PresenceService presenceService) {
        this.sessionService = sessionService;
        this.presenceService = presenceService;
    }

    // تنظيف الجلسات غير النشطة كل 5 دقائق
    // تنظيف الجلسات غير النشطة كل 5 دقائق
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cleanupInactiveSessions() {
        try {
            OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(5);
            sessionService.findInactiveSessions(threshold).forEach(session -> {
                log.debug("Cleaning up inactive session: {}", session.getId());
                sessionService.clearWebsocketSessionId(session.getId());
                presenceService.updateOnlineStatus(
                        session.getUser().getId(),
                        session.getId(),
                        false,
                        null
                );
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
                    // يمكنك إرسال رسالة heartbeat هنا إذا لزم الأمر
                    log.debug("Sending heartbeat to session: {}", session.getId());

                    // مثال: إرسال رسالة heartbeat عبر WebSocket
                    // webSocketService.sendHeartbeat(session.getWebsocketSessionId());
                }
            });
        } catch (Exception e) {
            log.error("Failed to send heartbeats: {}", e.getMessage());
        }
    }
}
