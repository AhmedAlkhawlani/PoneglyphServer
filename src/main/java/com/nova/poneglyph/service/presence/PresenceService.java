package com.nova.poneglyph.service.presence;

import com.nova.poneglyph.domain.user.UserSession;
import com.nova.poneglyph.repository.UserSessionRepository;
import com.nova.poneglyph.service.SessionService;
import com.nova.poneglyph.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final UserSessionRepository sessionRepository;
    private final SessionService sessionService;
    private final RedisTemplate<String, String> redisTemplate;
    private final WebSocketService webSocketService;

    private static final String PRESENCE_KEY = "presence:user:";
    private static final String SESSION_KEY = "presence:session:";
    private static final Duration ONLINE_TIMEOUT = Duration.ofMinutes(5);

    @Transactional
    public void updateOnlineStatus(UUID userId, UUID sessionId, boolean online, String websocketSessionId) {
        Optional<UserSession> sessionOpt = sessionService.getSessionById(sessionId);

        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setOnline(online);
            session.setLastActivity(OffsetDateTime.now());

            if (websocketSessionId != null) {
                session.setWebsocketSessionId(websocketSessionId);
            }

            sessionRepository.save(session);

            // تحديث حالة الحضور في Redis
            if (online) {
                redisTemplate.opsForValue().set(
                        PRESENCE_KEY + userId,
                        "online",
                        ONLINE_TIMEOUT
                );
                if (websocketSessionId != null) {
                    redisTemplate.opsForValue().set(
                            SESSION_KEY + sessionId,
                            websocketSessionId,
                            ONLINE_TIMEOUT
                    );
                }
            } else {
                redisTemplate.delete(PRESENCE_KEY + userId);
                redisTemplate.delete(SESSION_KEY + sessionId);
            }

            // إشعار المتصلين بتغيير حالة الحضور
            webSocketService.notifyPresenceChange(userId, online);
        }
    }

    public boolean isUserOnline(UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY + userId));
    }

    public Optional<String> getWebSocketSessionId(UUID sessionId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(SESSION_KEY + sessionId));
    }

    @Transactional
    public void heartbeat(UUID userId, UUID sessionId) {
        sessionService.getSessionById(sessionId).ifPresent(session -> {
            session.setLastActivity(OffsetDateTime.now());
            sessionRepository.save(session);

            // تجديد وقت انتهاء الصلاحية في Redis
            redisTemplate.expire(PRESENCE_KEY + userId, ONLINE_TIMEOUT);
            redisTemplate.expire(SESSION_KEY + sessionId, ONLINE_TIMEOUT);
        });
    }

    public OffsetDateTime getLastActive(UUID userId) {
        return sessionRepository.findByUser_Id(userId)
                .map(UserSession::getLastActivity)
                .orElse(null);
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

    // طريقة لتنظيف الجلسات المنتهية
    @Transactional
    public void cleanupExpiredSessions() {
        OffsetDateTime threshold = OffsetDateTime.now().minus(ONLINE_TIMEOUT);
        sessionRepository.findInactiveSessions(threshold).forEach(session -> {
            updateOnlineStatus(session.getUser().getId(), session.getId(), false, null);
        });
    }
}
