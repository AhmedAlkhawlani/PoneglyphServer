//
//
//package com.nova.poneglyph.service.presence;
//
//import com.nova.poneglyph.domain.user.UserSession;
//import com.nova.poneglyph.repository.UserSessionRepository;
//import com.nova.poneglyph.service.SessionService;
//import com.nova.poneglyph.service.WebSocketService;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.dao.DataAccessException;
//import org.springframework.data.redis.core.RedisOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.SessionCallback;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Duration;
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//
//@Service
//@RequiredArgsConstructor
//public class PresenceService {
//
//    private final Logger log = LoggerFactory.getLogger(PresenceService.class);
//    private final UserSessionRepository sessionRepository;
//    private final SessionService sessionService;
//    private final RedisTemplate<String, String> redisTemplate;
//    private final WebSocketService webSocketService;
//
//    private static final String PRESENCE_KEY = "presence:user:";
//    private static final String SESSION_KEY = "presence:session:";
//    // في PresenceService
//    private static final Duration ONLINE_TIMEOUT = Duration.ofSeconds(30); // تقليل المهلة
//
////    @Transactional
////    public void updateOnlineStatus(UUID userId, UUID sessionId, boolean online, String websocketSessionId) {
////        // استخدام معالجة دفعة لأداء أفضل
////        sessionService.getSessionById(sessionId).ifPresent(session -> {
////            session.setOnline(online);
////            session.setLastActivity(OffsetDateTime.now());
////
////            if (websocketSessionId != null) {
////                session.setWebsocketSessionId(websocketSessionId);
////            }
////
////            // تحديث Redis بشكل غير متزامن
////            CompletableFuture.runAsync(() -> {
////                if (online) {
////                    redisTemplate.opsForValue().set(
////                            PRESENCE_KEY + userId,
////                            "online",
////                            ONLINE_TIMEOUT
////                    );
////                } else {
////                    redisTemplate.delete(PRESENCE_KEY + userId);
////                }
////            });
////
////            // إرسال الإشعار بشكل غير متزامن
////            CompletableFuture.runAsync(() -> {
////                webSocketService.notifyPresenceChange(userId, online);
////            });
////        });
////    }
//@Transactional
//public void updateOnlineStatus(UUID userId, UUID sessionId, boolean online, String websocketSessionId) {
//    Optional<UserSession> sessionOpt = sessionService.getSessionById(sessionId);
//
//    if (sessionOpt.isPresent()) {
//        UserSession session = sessionOpt.get();
//        session.setOnline(online);
//        session.setLastActivity(OffsetDateTime.now());
//
//        if (websocketSessionId != null) {
//            session.setWebsocketSessionId(websocketSessionId);
//        }
//
//        sessionRepository.save(session);
//
//        // استخدام معاملات Redis للتأكد من التحديث المتسق
//        redisTemplate.execute(new SessionCallback<List<Object>>() {
//            @Override
//            public List<Object> execute(RedisOperations operations) throws DataAccessException {
//                operations.multi();
//
//                if (online) {
//                    operations.opsForValue().set(
//                            PRESENCE_KEY + userId,
//                            "online",
//                            ONLINE_TIMEOUT
//                    );
//                    if (websocketSessionId != null) {
//                        operations.opsForValue().set(
//                                SESSION_KEY + sessionId,
//                                websocketSessionId,
//                                ONLINE_TIMEOUT
//                        );
//                    }
//                } else {
//                    operations.delete(PRESENCE_KEY + userId);
//                    operations.delete(SESSION_KEY + sessionId);
//                }
//
//                return operations.exec();
//            }
//        });
//
//        // إرسال الإشعار بشكل غير متزامن مع معالجة الأخطاء
//        CompletableFuture.runAsync(() -> {
//            try {
//                webSocketService.notifyPresenceChange(userId, online);
//            } catch (Exception e) {
//                log.error("Failed to notify presence change for user {}: {}", userId, e.getMessage());
//            }
//        });
//    }
//}
//    public boolean isUserOnline(UUID userId) {
//        return Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY + userId));
//    }
//
//    public Optional<String> getWebSocketSessionId(UUID sessionId) {
//        return Optional.ofNullable(redisTemplate.opsForValue().get(SESSION_KEY + sessionId));
//    }
//
//    @Transactional
//    public void heartbeat(UUID userId, UUID sessionId) {
//        sessionService.getSessionById(sessionId).ifPresent(session -> {
//            session.setLastActivity(OffsetDateTime.now());
//            sessionRepository.save(session);
//
//            // تجديد وقت انتهاء الصلاحية في Redis
//            redisTemplate.expire(PRESENCE_KEY + userId, ONLINE_TIMEOUT);
//            redisTemplate.expire(SESSION_KEY + sessionId, ONLINE_TIMEOUT);
//        });
//    }
//
//    @Transactional
//    public void handleHeartbeat(UUID userId, UUID sessionId) {
//        // تجديد النشاط في Redis وقاعدة البيانات
//        sessionService.getSessionById(sessionId).ifPresent(session -> {
//            session.setLastActivity(OffsetDateTime.now());
//            sessionRepository.save(session);
//        });
//
//        // تجديد وقت انتهاء الصلاحية في Redis
//        redisTemplate.expire(PRESENCE_KEY + userId, ONLINE_TIMEOUT);
//        redisTemplate.expire(SESSION_KEY + sessionId, ONLINE_TIMEOUT);
//
//        log.debug("Heartbeat processed for user: {}, session: {}", userId, sessionId);
//    }
//
//    public OffsetDateTime getLastActive(UUID userId) {
//        return sessionRepository.findByUser_Id(userId)
//                .map(UserSession::getLastActivity)
//                .orElse(null);
//    }
//
//    public OffsetDateTime getLastActiveForPhone(String normalizedPhone) {
//        return sessionRepository.findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(normalizedPhone)
//                .map(UserSession::getLastActivity)
//                .orElse(null);
//    }
//
//    public boolean isUserOnlineForPhone(String normalizedPhone) {
//        return sessionRepository.findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(normalizedPhone)
//                .map(session -> Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY + session.getUser().getId())))
//                .orElse(false);
//    }
//
//    // طريقة لمراقبة جودة الاتصال
//    public Map<String, Object> getConnectionQuality(UUID userId) {
//        Optional<UserSession> sessionOpt = sessionService.findLatestActiveSessionByUserId(userId);
//        if (!sessionOpt.isPresent()) {
//            return Map.of("status", "offline");
//        }
//
//        UserSession session = sessionOpt.get();
//        OffsetDateTime lastActivity = session.getLastActivity();
//        long minutesSinceLastActivity = Duration.between(lastActivity, OffsetDateTime.now()).toMinutes();
//
//        boolean redisActive = Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY + userId));
//
//        return Map.of(
//                "status", redisActive ? "online" : "offline",
//                "lastActivity", lastActivity,
//                "minutesSinceActivity", minutesSinceLastActivity,
//                "websocketSessionId", session.getWebsocketSessionId(),
//                "sessionActive", session.isOnline()
//        );
//    }
//
//    // طريقة لتنظيف الجلسات المنتهية
//    @Transactional
//    public void cleanupExpiredSessions() {
//        OffsetDateTime threshold = OffsetDateTime.now().minus(ONLINE_TIMEOUT);
//        sessionRepository.findInactiveSessions(threshold).forEach(session -> {
//            updateOnlineStatus(session.getUser().getId(), session.getId(), false, null);
//        });
//    }
//
//    @Transactional
//    public void updateOnlineStatusImmediate(UUID userId, UUID sessionId, boolean online, String websocketSessionId) {
//        // تحديث Redis فورياً
//        if (online) {
//            redisTemplate.opsForValue().set(PRESENCE_KEY + userId, "online", ONLINE_TIMEOUT);
//            if (websocketSessionId != null) {
//                redisTemplate.opsForValue().set(SESSION_KEY + sessionId, websocketSessionId, ONLINE_TIMEOUT);
//            }
//        } else {
//            redisTemplate.delete(PRESENCE_KEY + userId);
//            redisTemplate.delete(SESSION_KEY + sessionId);
//        }
//
//        // إرسال إشعار مباشر بدون CompletableFuture
//        try {
//            webSocketService.notifyPresenceChange(userId, online);
//        } catch (Exception e) {
//            log.error("Failed to notify presence change for user {}: {}", userId, e.getMessage());
//        }
//    }
//
////}
//package com.nova.poneglyph.service.presence;
//
//import com.nova.poneglyph.domain.user.UserSession;
//import com.nova.poneglyph.repository.UserSessionRepository;
//import com.nova.poneglyph.service.SessionService;
//import com.nova.poneglyph.service.WebSocketService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Duration;
//import java.time.OffsetDateTime;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//public class PresenceService {
//
//    private final Logger log = LoggerFactory.getLogger(PresenceService.class);
//
//    private final SessionService sessionService;
//    private final RedisTemplate<String, String> redisTemplate;
//    private final WebSocketService webSocketService;
//
//    private static final String PRESENCE_KEY = "presence:user:";
//    private static final String SESSION_KEY = "presence:session:";
//    private static final Duration ONLINE_TIMEOUT = Duration.ofSeconds(60); // ضبط timeout حسب حاجتك
//
//    public PresenceService(SessionService sessionService,
//                           RedisTemplate<String, String> redisTemplate,
//                           WebSocketService webSocketService, UserSessionRepository sessionRepository) {
//        this.sessionService = sessionService;
//        this.redisTemplate = redisTemplate;
//        this.webSocketService = webSocketService;
//        this.sessionRepository = sessionRepository;
//    }
//
//    /**
//     * تحديث فوري للحالة — يحدث في نفس الـ thread
//     * يحدث في DB و Redis و يرسل إشعار presence مباشرة.
//     */
//    @Transactional
//    public void updateOnlineStatusImmediate(UUID userId, UUID sessionId, boolean online, String websocketSessionId) {
//        try {
//            Optional<UserSession> sessionOpt = sessionService.getSessionById(sessionId);
//            sessionOpt.ifPresent(session -> {
//                session.setOnline(online);
//                session.setLastActivity(OffsetDateTime.now());
//                if (websocketSessionId != null) {
//                    session.setWebsocketSessionId(websocketSessionId);
//                } else if (!online) {
//                    session.setWebsocketSessionId(null);
//                }
//                sessionService.saveSession(session); // حفظ فوري في DB
//            });
//
//            // تحديث Redis فوري
//            String presenceKey = PRESENCE_KEY + userId;
//            String sessionKey = SESSION_KEY + sessionId;
//
//            if (online) {
//                redisTemplate.opsForValue().set(presenceKey, "online");
//                redisTemplate.expire(presenceKey, ONLINE_TIMEOUT);
//                if (websocketSessionId != null) {
//                    redisTemplate.opsForValue().set(sessionKey, websocketSessionId);
//                    redisTemplate.expire(sessionKey, ONLINE_TIMEOUT);
//                }
//            } else {
//                redisTemplate.delete(presenceKey);
//                redisTemplate.delete(sessionKey);
//            }
//
//            // إرسال إشعار فوراً عبر WebSocket (synchronous)
//            webSocketService.notifyPresenceChange(userId, online);
//
//            log.debug("Presence updated immediate for user={}, session={}, online={}", userId, sessionId, online);
//        } catch (Exception e) {
//            log.error("Failed to updateOnlineStatusImmediate for user {}: {}", userId, e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    /**
//     * تجديد heartbeat فوري: يجدد lastActivity في DB ويمدد صلاحية المفاتيح في Redis.
//     */
//    @Transactional
//    public void handleHeartbeatImmediate(UUID userId, UUID sessionId) {
//        try {
//            sessionService.getSessionById(sessionId).ifPresent(session -> {
//                session.setLastActivity(OffsetDateTime.now());
//                sessionService.saveSession(session);
//            });
//
//            String presenceKey = PRESENCE_KEY + userId;
//            String sessionKey = SESSION_KEY + sessionId;
//
//            // فقط مد المهلة إن كان المفتاح موجودًا (يعني المستخدم يعتبر متصل)
//            if (Boolean.TRUE.equals(redisTemplate.hasKey(presenceKey))) {
//                redisTemplate.expire(presenceKey, ONLINE_TIMEOUT);
//            }
//            if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
//                redisTemplate.expire(sessionKey, ONLINE_TIMEOUT);
//            }
//
//            // إرسال إشعار فوراً عبر WebSocket (synchronous)
//            webSocketService.notifyPresenceChange(userId, true);
//            log.debug("Heartbeat immediate processed for user={}, session={}", userId, sessionId);
//        } catch (Exception e) {
//            log.error("Failed to handleHeartbeatImmediate for user {}: {}", userId, e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    public boolean isUserOnline(UUID userId) {
//        try {
//            return Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY + userId));
//        } catch (Exception e) {
//            log.warn("isUserOnline failed for {}: {}", userId, e.getMessage());
//            return false;
//        }
//    }
//
//    public Optional<String> getWebSocketSessionId(UUID sessionId) {
//        try {
//            return Optional.ofNullable(redisTemplate.opsForValue().get(SESSION_KEY + sessionId));
//        } catch (Exception e) {
//            log.warn("getWebSocketSessionId failed for {}: {}", sessionId, e.getMessage());
//            return Optional.empty();
//        }
//    }
//
//    // تنظيف جلسة واحدة فوريًا
//    @Transactional
//    public void markSessionOfflineImmediate(UUID sessionId) {
//        sessionService.findById(sessionId).ifPresent(session -> {
//            UUID userId = session.getUser().getId();
//            session.setOnline(false);
//            session.setWebsocketSessionId(null);
//            sessionService.saveSession(session);
//
//            redisTemplate.delete(PRESENCE_KEY + userId);
//            redisTemplate.delete(SESSION_KEY + sessionId);
//
//            webSocketService.notifyPresenceChange(userId, false);
//            log.info("Marked session offline immediate: {}", sessionId);
//        });
//    }
//        public OffsetDateTime getLastActiveForPhone(String normalizedPhone) {
//        return sessionRepository.findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(normalizedPhone)
//                .map(UserSession::getLastActivity)
//                .orElse(null);
//    }
//        public boolean isUserOnlineForPhone(String normalizedPhone) {
//        return sessionRepository.findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(normalizedPhone)
//                .map(session -> Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY + session.getUser().getId())))
//                .orElse(false);
//    }
////
//    private final UserSessionRepository sessionRepository;
////
//}

package com.nova.poneglyph.service.presence;

import com.nova.poneglyph.domain.user.UserSession;
import com.nova.poneglyph.repository.UserSessionRepository;
import com.nova.poneglyph.service.SessionService;
import com.nova.poneglyph.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PresenceService {

    private final Logger log = LoggerFactory.getLogger(PresenceService.class);

    private final SessionService sessionService;
    private final RedisTemplate<String, String> redisTemplate;
    private final WebSocketService webSocketService;
    private final UserSessionRepository sessionRepository;

    private static final String PRESENCE_KEY = "presence:user:";
    private static final String SESSION_KEY = "presence:session:";
    private static final Duration ONLINE_TIMEOUT = Duration.ofSeconds(60);

    public PresenceService(SessionService sessionService,
                           RedisTemplate<String, String> redisTemplate,
                           WebSocketService webSocketService,
                           UserSessionRepository sessionRepository) {
        this.sessionService = sessionService;
        this.redisTemplate = redisTemplate;
        this.webSocketService = webSocketService;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public void updateOnlineStatusImmediate(UUID userId, UUID sessionId, boolean online, String websocketSessionId) {
        try {
            Optional<UserSession> sessionOpt = sessionService.getSessionById(sessionId);
            sessionOpt.ifPresent(session -> {
                session.setOnline(online);
                session.setLastActivity(OffsetDateTime.now());
                if (websocketSessionId != null) {
                    session.setWebsocketSessionId(websocketSessionId);
                } else if (!online) {
                    session.setWebsocketSessionId(null);
                }
                sessionService.saveSession(session);
            });

            String presenceKey = PRESENCE_KEY + userId;
            String sessionKey = SESSION_KEY + sessionId;

            if (online) {
                redisTemplate.opsForValue().set(presenceKey, "online");
                redisTemplate.expire(presenceKey, ONLINE_TIMEOUT);
                if (websocketSessionId != null) {
                    redisTemplate.opsForValue().set(sessionKey, websocketSessionId);
                    redisTemplate.expire(sessionKey, ONLINE_TIMEOUT);
                }
            } else {
                redisTemplate.delete(presenceKey);
                redisTemplate.delete(sessionKey);
            }

            webSocketService.notifyPresenceChange(userId, online);
            log.debug("Presence updated immediate for user={}, session={}, online={}", userId, sessionId, online);
        } catch (Exception e) {
            log.error("Failed to updateOnlineStatusImmediate for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void handleHeartbeatImmediate(UUID userId, UUID sessionId) {
        try {
            sessionService.getSessionById(sessionId).ifPresent(session -> {
                session.setLastActivity(OffsetDateTime.now());
                sessionService.saveSession(session);
            });

            String presenceKey = PRESENCE_KEY + userId;
            String sessionKey = SESSION_KEY + sessionId;

            if (Boolean.TRUE.equals(redisTemplate.hasKey(presenceKey))) {
                redisTemplate.expire(presenceKey, ONLINE_TIMEOUT);
            }
            if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
                redisTemplate.expire(sessionKey, ONLINE_TIMEOUT);
            }

            webSocketService.notifyPresenceChange(userId, true);
            log.debug("Heartbeat immediate processed for user={}, session={}", userId, sessionId);
        } catch (Exception e) {
            log.error("Failed to handleHeartbeatImmediate for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean isUserOnline(UUID userId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY + userId));
        } catch (Exception e) {
            log.warn("isUserOnline failed for {}: {}", userId, e.getMessage());
            return false;
        }
    }

    public Optional<String> getWebSocketSessionId(UUID sessionId) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(SESSION_KEY + sessionId));
        } catch (Exception e) {
            log.warn("getWebSocketSessionId failed for {}: {}", sessionId, e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional
    public void markSessionOfflineImmediate(UUID sessionId) {
        sessionService.findById(sessionId).ifPresent(session -> {
            UUID userId = session.getUser().getId();
            session.setOnline(false);
            session.setWebsocketSessionId(null);
            sessionService.saveSession(session);

            redisTemplate.delete(PRESENCE_KEY + userId);
            redisTemplate.delete(SESSION_KEY + sessionId);

            webSocketService.notifyPresenceChange(userId, false);
            log.info("Marked session offline immediate: {}", sessionId);
        });
    }

    public OffsetDateTime getLastActiveForPhone(String normalizedPhone) {
        return sessionRepository.findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(normalizedPhone)
                .map(UserSession::getLastActivity)
                .orElse(null);
    }

    public boolean isUserOnlineForPhone(String normalizedPhone) {
        return sessionRepository.findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(normalizedPhone)
                .map(session -> Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY + session.getUser().getId())))
                .orElse(false);
    }
}
