package com.nova.poneglyph.service.presence;



import com.nova.poneglyph.domain.user.UserSession;
import com.nova.poneglyph.repository.UserSessionRepository;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final WebSocketService webSocketService;

    private static final String PRESENCE_KEY = "presence";
    private static final Duration ONLINE_TIMEOUT = Duration.ofMinutes(5);

//    @Transactional
//    public void updateOnlineStatus(UUID userId, String sessionId, boolean online) {
//        sessionRepository.findByUserId(userId).ifPresent(session -> {
//            session.setOnline(online);
//            session.setLastActivity(OffsetDateTime.now());
//            session.setWebsocketSessionId(sessionId);
//            sessionRepository.save(session);
//
//            // Update Redis presence
//            if (online) {
//                redisTemplate.opsForValue().set(
//                        PRESENCE_KEY + ":" + userId,
//                        "online",
//                        ONLINE_TIMEOUT
//                );
//            } else {
//                redisTemplate.delete(PRESENCE_KEY + ":" + userId);
//            }
//
//            // Notify contacts
//            webSocketService.notifyPresenceChange(userId, online);
//        });
//    }
//
//    public boolean isUserOnline(UUID userId) {
//        return Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY + ":" + userId));
//    }
//
//    public OffsetDateTime getLastActive(UUID userId) {
//        return sessionRepository.findByUserId(userId)
//                .map(UserSession::getLastActivity)
//                .orElse(null);
//    }
//
//    @Transactional
//    public void heartbeat(UUID userId) {
//        sessionRepository.findByUserId(userId).ifPresent(session -> {
//            session.setLastActivity(OffsetDateTime.now());
//            sessionRepository.save(session);
//            redisTemplate.expire(
//                    PRESENCE_KEY + ":" + userId,
//                    ONLINE_TIMEOUT
//            );
//        });
//    }

    // يعيد آخر نشاط للمستخدم بناءً على رقم الهاتف
    public OffsetDateTime getLastActiveForPhone(String normalizedPhone) {
        return sessionRepository.findByUser_NormalizedPhone(normalizedPhone)
                .map(UserSession::getLastActivity)
                .orElse(null);
    }

    // يتحقق مما إذا كان المستخدم متصلًا بالاعتماد على رقم الهاتف
    public boolean isUserOnlineForPhone(String normalizedPhone) {
        return sessionRepository.findByUser_NormalizedPhone(normalizedPhone)
                .map(session -> Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY + ":" + session.getUser().getId())))
                .orElse(false);
    }

}
