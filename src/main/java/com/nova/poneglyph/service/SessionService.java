//package com.nova.poneglyph.service;
//
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.domain.user.UserDevice;
//import com.nova.poneglyph.domain.user.UserSession;
//import com.nova.poneglyph.repository.RefreshTokenRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.repository.UserSessionRepository;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class SessionService {
//
//    private final Logger log = LoggerFactory.getLogger(SessionService.class);
//    private final UserSessionRepository sessionRepository;
//    private final UserRepository userRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
//
//    /**
//     * إنشاء جلسة جديدة — يتم إبطال الجلسات القديمة فوراً.
//     */
//    @Transactional
//    public UserSession createSession(UUID userId, UserDevice device, String refreshTokenHash, String ip) {
//        revokeAllSessions(userId);
//
//        User user = userRepository.getReferenceById(userId);
//
//        UserSession session = UserSession.builder()
//                .user(user)
//                .device(device)
//                .refreshTokenHash(refreshTokenHash)
//                .issuedAt(OffsetDateTime.now())
//                .lastUsedAt(OffsetDateTime.now())
//                .lastActivity(OffsetDateTime.now())
//                .expiresAt(OffsetDateTime.now().plusDays(30))
//                .ipAddress(ip)
//                .active(true)
//                .online(false)
//                .build();
//
//        UserSession saved = sessionRepository.save(session);
//        log.debug("Created new session {} for user {}", saved.getId(), userId);
//        return saved;
//    }
//
//    /**
//     * حفظ الجلسة فورياً (مستخدم من PresenceService).
//     */
//    @Transactional
//    public UserSession saveSession(UserSession session) {
//        UserSession saved = sessionRepository.save(session);
//        log.debug("Saved session {} for user {}", session.getId(), session.getUser().getId());
//        return saved;
//    }
//
//    @Transactional
//    public void revokeSession(UUID sessionId) {
//        sessionRepository.findById(sessionId).ifPresent(session -> {
//            session.revoke();
//            sessionRepository.save(session);
//            log.debug("Revoked session {}", sessionId);
//        });
//    }
//
//    @Transactional
//    public void revokeSessionByJti(UUID jti) {
//        sessionRepository.findByActiveJti(jti).ifPresent(session -> {
//            session.setActive(false);
//            session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            session.setActiveJti(null);
//            sessionRepository.save(session);
//            log.debug("Session fully revoked for jti: {}", jti);
//        });
//    }
//
//    @Transactional
//    public void revokeAllSessions(UUID userId) {
//        sessionRepository.revokeAllForUser(userId);
//        log.debug("Revoked all sessions for user {}", userId);
//    }
//
//    @Transactional
//    public void revokeAllActiveSessionsAndTokens(UUID userId) {
//        sessionRepository.revokeAllActiveSessionsForUser(userId);
//        log.debug("Revoked all active sessions and tokens for user {}", userId);
//    }
//
//    @Transactional(readOnly = true)
//    public List<UserSession> getActiveSessions(UUID userId) {
//        return sessionRepository.findByUser_IdAndActiveTrue(userId);
//    }
//
//    /**
//     * إرجاع جلسة بواسطة معرف الجلسة.
//     * يوجد لديك أيضاً getSessionById (alias) حتى لا يكسر التوافق مع الكود الحالي.
//     */
//    @Transactional(readOnly = true)
//    public Optional<UserSession> getSessionById(UUID sessionId) {
//        return sessionRepository.findById(sessionId);
//    }
//
//    /**
//     * Alias يتماشى مع النداءات الأخرى (findById).
//     */
//    @Transactional(readOnly = true)
//    public Optional<UserSession> findById(UUID sessionId) {
//        return getSessionById(sessionId);
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<UserSession> findByUserAndDevice(User user, UserDevice device) {
//        return sessionRepository.findByUserAndDevice(user, device);
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<UserSession> findLatestActiveSessionByUserId(UUID userId) {
//        return sessionRepository.findTop1ByUser_IdAndActiveTrueOrderByLastUsedAtDesc(userId);
//    }
//
//    @Transactional(readOnly = true)
//    public List<UserSession> findActiveSessionsByUserId(UUID userId) {
//        return sessionRepository.findActiveSessionsByUserId(userId);
//    }
//
//    /**
//     * تحديث نشاط الجلسة (lastUsedAt و lastActivity) وحفظها فورياً.
//     */
//    @Transactional
//    public void updateSessionActivity(UUID sessionId) {
//        sessionRepository.findById(sessionId).ifPresent(session -> {
//            session.setLastUsedAt(OffsetDateTime.now());
//            session.setLastActivity(OffsetDateTime.now());
//            sessionRepository.save(session);
//            log.debug("Updated activity for session {}", sessionId);
//        });
//    }
//
//    @Transactional
//    public void updateOnlineStatus(UUID sessionId, boolean online) {
//        sessionRepository.updateOnlineStatus(sessionId, online);
//        log.debug("Updated online status (db) for session {} -> {}", sessionId, online);
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<UserSession> findByWebsocketSessionId(String websocketSessionId) {
//        return sessionRepository.findByWebsocketSessionId(websocketSessionId);
//    }
//
//    /**
//     * تعيين websocketSessionId وحفظ الجلسة فوراً، ووضعها online في DB.
//     * هذا تنفيذ فوري متوافق مع تغييرات presence.
//     */
//    @Transactional
//    public void setWebsocketSessionId(UUID sessionId, String websocketSessionId) {
//        sessionRepository.findById(sessionId).ifPresent(session -> {
//            session.setWebsocketSessionId(websocketSessionId);
//            session.setOnline(true);
//            session.setLastActivity(OffsetDateTime.now());
//            sessionRepository.save(session);
//            log.debug("Set websocketSessionId {} for session {}", websocketSessionId, sessionId);
//        });
//    }
//
//    /**
//     * مسح websocketSessionId ووضع offline فورياً وحفظ التغييرات.
//     */
//    @Transactional
//    public void clearWebsocketSessionId(UUID sessionId) {
//        sessionRepository.findById(sessionId).ifPresent(session -> {
//            session.setWebsocketSessionId(null);
//            session.setOnline(false);
//            session.setLastActivity(OffsetDateTime.now());
//            sessionRepository.save(session);
//            log.debug("Cleared websocketSessionId and set offline for session {}", sessionId);
//        });
//    }
//
//    @Transactional(readOnly = true)
//    public List<UserSession> findInactiveSessions(OffsetDateTime threshold) {
//        return sessionRepository.findInactiveSessions(threshold);
//    }
//
//    @Transactional
//    public void revokeSessionAndTokensByJti(UUID jti) {
//        sessionRepository.findByActiveJti(jti).ifPresent(session -> {
//            session.setActive(false);
//            session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            session.setActiveJti(null);
//            sessionRepository.save(session);
//
//            refreshTokenRepository.findByJti(jti).ifPresent(token -> {
//                token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//                refreshTokenRepository.save(token);
//            });
//
//            log.debug("Session and associated tokens revoked for jti: {}", jti);
//        });
//    }
//
//    @Transactional(readOnly = true)
//    public List<UserSession> getAllActiveSessions() {
//        return sessionRepository.findAllActiveSessions();
//    }
////}
//package com.nova.poneglyph.service;
//
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.domain.user.UserDevice;
//import com.nova.poneglyph.domain.user.UserSession;
//import com.nova.poneglyph.repository.RefreshTokenRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.repository.UserSessionRepository;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.dao.OptimisticLockingFailureException;
//import org.springframework.orm.ObjectOptimisticLockingFailureException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.support.TransactionCallback;
//import org.springframework.transaction.support.TransactionTemplate;
//
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//public class SessionService {
//
//    private final Logger log = LoggerFactory.getLogger(SessionService.class);
//    private final UserSessionRepository sessionRepository;
//    private final UserRepository userRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final PlatformTransactionManager transactionManager;
//
//    // TransactionTemplate يُستخدم لإنشاء معاملة جديدة لكل محاولة في آلية الـ retry
//    private final TransactionTemplate transactionTemplate;
//
//    public SessionService(UserSessionRepository sessionRepository,
//                          UserRepository userRepository,
//                          RefreshTokenRepository refreshTokenRepository,
//                          PlatformTransactionManager transactionManager) {
//        this.sessionRepository = sessionRepository;
//        this.userRepository = userRepository;
//        this.refreshTokenRepository = refreshTokenRepository;
//        this.transactionManager = transactionManager;
//        this.transactionTemplate = new TransactionTemplate(transactionManager);
//    }
//
//    /**
//     * إنشاء جلسة جديدة — يتم إبطال الجلسات القديمة فوراً.
//     */
//    @Transactional
//    public UserSession createSession(UUID userId, UserDevice device, String refreshTokenHash, String ip) {
//        revokeAllSessions(userId);
//
//        User user = userRepository.getReferenceById(userId);
//
//        UserSession session = UserSession.builder()
//                .user(user)
//                .device(device)
//                .refreshTokenHash(refreshTokenHash)
//                .issuedAt(OffsetDateTime.now())
//                .lastUsedAt(OffsetDateTime.now())
//                .lastActivity(OffsetDateTime.now())
//                .expiresAt(OffsetDateTime.now().plusDays(30))
//                .ipAddress(ip)
//                .active(true)
//                .online(false)
//                .build();
//
//        UserSession saved = sessionRepository.save(session);
//        log.debug("Created new session {} for user {}", saved.getId(), userId);
//        return saved;
//    }
//
//    @Transactional
//    public UserSession saveSession(UserSession session) {
//        UserSession saved = sessionRepository.save(session);
//        log.debug("Saved session {} for user {}", session.getId(), session.getUser().getId());
//        return saved;
//    }
//
//    @Transactional
//    public void revokeSession(UUID sessionId) {
//        sessionRepository.findById(sessionId).ifPresent(session -> {
//            session.revoke();
//            sessionRepository.save(session);
//            log.debug("Revoked session {}", sessionId);
//        });
//    }
//
//    @Transactional
//    public void revokeSessionByJti(UUID jti) {
//        sessionRepository.findByActiveJti(jti).ifPresent(session -> {
//            session.setActive(false);
//            session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            session.setActiveJti(null);
//            sessionRepository.save(session);
//            log.debug("Session fully revoked for jti: {}", jti);
//        });
//    }
//
//    @Transactional
//    public void revokeAllSessions(UUID userId) {
//        sessionRepository.revokeAllForUser(userId);
//        log.debug("Revoked all sessions for user {}", userId);
//    }
//
//    @Transactional
//    public void revokeAllActiveSessionsAndTokens(UUID userId) {
//        sessionRepository.revokeAllActiveSessionsForUser(userId);
//        log.debug("Revoked all active sessions and tokens for user {}", userId);
//    }
//
//    @Transactional(readOnly = true)
//    public List<UserSession> getActiveSessions(UUID userId) {
//        return sessionRepository.findByUser_IdAndActiveTrue(userId);
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<UserSession> getSessionById(UUID sessionId) {
//        return sessionRepository.findById(sessionId);
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<UserSession> findById(UUID sessionId) {
//        return getSessionById(sessionId);
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<UserSession> findByUserAndDevice(User user, UserDevice device) {
//        return sessionRepository.findByUserAndDevice(user, device);
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<UserSession> findLatestActiveSessionByUserId(UUID userId) {
//        return sessionRepository.findTop1ByUser_IdAndActiveTrueOrderByLastUsedAtDesc(userId);
//    }
//
//    @Transactional(readOnly = true)
//    public List<UserSession> findActiveSessionsByUserId(UUID userId) {
//        return sessionRepository.findActiveSessionsByUserId(userId);
//    }
//
//    @Transactional
//    public void updateSessionActivity(UUID sessionId) {
//        sessionRepository.findById(sessionId).ifPresent(session -> {
//            session.setLastUsedAt(OffsetDateTime.now());
//            session.setLastActivity(OffsetDateTime.now());
//            sessionRepository.save(session);
//            log.debug("Updated activity for session {}", sessionId);
//        });
//    }
//
//    @Transactional
//    public void updateOnlineStatus(UUID sessionId, boolean online) {
//        sessionRepository.updateOnlineStatus(sessionId, online);
//        log.debug("Updated online status (db) for session {} -> {}", sessionId, online);
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<UserSession> findByWebsocketSessionId(String websocketSessionId) {
//        return sessionRepository.findByWebsocketSessionId(websocketSessionId);
//    }
//
//    /**
//     * تعيين websocketSessionId وحفظ الجلسة فوراً، ووضعها online في DB.
//     */
//    @Transactional
//    public void setWebsocketSessionId(UUID sessionId, String websocketSessionId) {
//        sessionRepository.findById(sessionId).ifPresent(session -> {
//            session.setWebsocketSessionId(websocketSessionId);
//            session.setOnline(true);
//            session.setLastActivity(OffsetDateTime.now());
//            sessionRepository.save(session);
//            log.debug("Set websocketSessionId {} for session {}", websocketSessionId, sessionId);
//        });
//    }
//
//    /**
//     * مسح websocketSessionId ووضع offline فورياً وحفظ التغييرات.
//     * هذه النسخة تحاول استخدام findById عادي؛ لكن في سيناريوهات السباق استخدم
//     * clearWebsocketSessionIdWithPessimisticLock أو clearWebsocketSessionIdWithRetry.
//     */
//    @Transactional
//    public void clearWebsocketSessionId(UUID sessionId) {
//        sessionRepository.findById(sessionId).ifPresent(session -> {
//            session.setWebsocketSessionId(null);
//            session.setOnline(false);
//            session.setLastActivity(OffsetDateTime.now());
//            sessionRepository.save(session);
//            log.debug("Cleared websocketSessionId and set offline for session {}", sessionId);
//        });
//    }
//
//    @Transactional(readOnly = true)
//    public List<UserSession> findInactiveSessions(OffsetDateTime threshold) {
//        return sessionRepository.findInactiveSessions(threshold);
//    }
//
//    @Transactional
//    public void revokeSessionAndTokensByJti(UUID jti) {
//        sessionRepository.findByActiveJti(jti).ifPresent(session -> {
//            session.setActive(false);
//            session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            session.setActiveJti(null);
//            sessionRepository.save(session);
//
//            refreshTokenRepository.findByJti(jti).ifPresent(token -> {
//                token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//                refreshTokenRepository.save(token);
//            });
//
//            log.debug("Session and associated tokens revoked for jti: {}", jti);
//        });
//    }
//
//    @Transactional(readOnly = true)
//    public List<UserSession> getAllActiveSessions() {
//        return sessionRepository.findAllActiveSessions();
//    }
//
//    // ----------------- New safe methods for disconnect/cleanup -----------------
//
//    /**
//     * استخدام قفل متشائم (pessimistic) عند مسح websocketSessionId.
//     * هذه الطريقة تفتح معاملة @Transactional وتستخدم findByIdForUpdate (PESSIMISTIC_WRITE).
//     */
//    @Transactional
//    public void clearWebsocketSessionIdWithPessimisticLock(UUID sessionId) {
//        // إذا لم يكن المستودع يدعم findByIdForUpdate ستعود Optional.empty
//        sessionRepository.findByIdForUpdate(sessionId).ifPresent(session -> {
//            session.setWebsocketSessionId(null);
//            session.setOnline(false);
//            session.setLastActivity(OffsetDateTime.now());
//            sessionRepository.save(session);
//            log.debug("Cleared websocketSessionId (pessimistic) and set offline for session {}", sessionId);
//        });
//    }
//
//    /**
//     * آلية إعادة المحاولة: كل محاولة تنفذ في معاملة جديدة عبر TransactionTemplate.
//     * مفيدة عندما لا تريد استخدام القفل المتشائم أو في حالات السباق العابرة.
//     */
//    public void clearWebsocketSessionIdWithRetry(UUID sessionId) {
//        int maxAttempts = 3;
//        int attempt = 0;
//
//        while (attempt < maxAttempts) {
//            attempt++;
//            try {
//                Boolean success = transactionTemplate.execute((TransactionCallback<Boolean>) status -> {
//                    Optional<UserSession> opt = sessionRepository.findById(sessionId);
//                    if (opt.isPresent()) {
//                        UserSession s = opt.get();
//                        s.setWebsocketSessionId(null);
//                        s.setOnline(false);
//                        s.setLastActivity(OffsetDateTime.now());
//                        sessionRepository.save(s);
//                        return Boolean.TRUE;
//                    } else {
//                        return Boolean.TRUE; // nothing to do
//                    }
//                });
//
//                if (Boolean.TRUE.equals(success)) {
//                    log.debug("Cleared websocketSessionId with retry for session {} on attempt {}", sessionId, attempt);
//                    return;
//                }
//            } catch ( OptimisticLockingFailureException lockEx) {
//                log.warn("Optimistic lock failure clearing websocketSessionId for {} (attempt {}/{}): {}", sessionId, attempt, maxAttempts, lockEx.getMessage());
//                // backoff قبل المحاولة التالية
//                try { Thread.sleep(50L * attempt); } catch (InterruptedException ignored) {}
//            } catch (Exception e) {
//                log.error("Unexpected error clearing websocketSessionId for {} (attempt {}/{}): {}", sessionId, attempt, maxAttempts, e.getMessage(), e);
//                // إذا كان استثناء غير متعلق بقفل، أعد المحاولة حسب السياسة أو اكسر
//                try { Thread.sleep(50L * attempt); } catch (InterruptedException ignored) {}
//            }
//        }
//
//        // فشل بعد المحاولات -> فالباك: نحاول تحديث أقل تكلفة وننبه النظام
//        log.error("Failed to clear websocketSessionId after {} attempts for session {}", maxAttempts, sessionId);
//        try {
//            transactionTemplate.execute((TransactionCallback<Void>) status -> {
//                sessionRepository.findById(sessionId).ifPresent(session -> {
//                    session.setWebsocketSessionId(null);
//                    session.setOnline(false);
//                    sessionRepository.save(session);
//                });
//                return null;
//            });
//        } catch (Exception e) {
//            log.warn("Fallback DB update also failed for session {}: {}", sessionId, e.getMessage());
//            // لا نرمي الاستثناء لأننا نفضل ألا يكسر ذلك تدفق الـ WebSocket/cleanup
//        }
//    }
//
//    // ----------------- end new methods -----------------
//}
package com.nova.poneglyph.service;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserDevice;
import com.nova.poneglyph.domain.user.UserSession;
import com.nova.poneglyph.repository.RefreshTokenRepository;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final Logger log = LoggerFactory.getLogger(SessionService.class);
    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PlatformTransactionManager transactionManager;

    private final TransactionTemplate transactionTemplate;



    @Transactional
    public UserSession createSession(UUID userId, UserDevice device, String refreshTokenHash, String ip) {
        revokeAllSessions(userId);

        User user = userRepository.getReferenceById(userId);

        UserSession session = UserSession.builder()
                .id(UUID.randomUUID())
                .user(user)
                .device(device)
                .refreshTokenHash(refreshTokenHash)
                .issuedAt(OffsetDateTime.now())
                .lastUsedAt(OffsetDateTime.now())
                .lastActivity(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusDays(30))
                .ipAddress(ip)
                .active(true)
                .online(false)
                .build();

        UserSession saved = sessionRepository.save(session);
        log.debug("Created new session {} for user {}", saved.getId(), userId);
        return saved;
    }

    @Transactional
    public UserSession saveSession(UserSession session) {
        UserSession saved = sessionRepository.save(session);
        log.debug("Saved session {} for user {}", session.getId(), session.getUser().getId());
        return saved;
    }

    @Transactional
    public void revokeSession(UUID sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.revoke();
            sessionRepository.save(session);
            log.debug("Revoked session {}", sessionId);
        });
    }

    @Transactional
    public void revokeSessionByJti(UUID jti) {
        sessionRepository.findByActiveJti(jti).ifPresent(session -> {
            session.setActive(false);
            session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
            session.setActiveJti(null);
            sessionRepository.save(session);
            log.debug("Session fully revoked for jti: {}", jti);
        });
    }

    @Transactional
    public void revokeAllSessions(UUID userId) {
        sessionRepository.revokeAllForUser(userId);
        log.debug("Revoked all sessions for user {}", userId);
    }

    @Transactional(readOnly = true)
    public List<UserSession> getActiveSessions(UUID userId) {
        return sessionRepository.findByUser_IdAndActiveTrue(userId);
    }

    @Transactional(readOnly = true)
    public Optional<UserSession> getSessionById(UUID sessionId) {
        return sessionRepository.findById(sessionId);
    }

    @Transactional(readOnly = true)
    public Optional<UserSession> findById(UUID sessionId) {
        return getSessionById(sessionId);
    }

    @Transactional(readOnly = true)
    public Optional<UserSession> findByUserAndDevice(User user, UserDevice device) {
        return sessionRepository.findByUserAndDevice(user, device);
    }

    @Transactional(readOnly = true)
    public Optional<UserSession> findLatestActiveSessionByUserId(UUID userId) {
        return sessionRepository.findTop1ByUser_IdAndActiveTrueOrderByLastUsedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<UserSession> findActiveSessionsByUserId(UUID userId) {
        return sessionRepository.findActiveSessionsByUserId(userId);
    }

    @Transactional
    public void updateSessionActivity(UUID sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setLastUsedAt(OffsetDateTime.now());
            session.setLastActivity(OffsetDateTime.now());
            sessionRepository.save(session);
            log.debug("Updated activity for session {}", sessionId);
        });
    }

    @Transactional
    public void updateOnlineStatus(UUID sessionId, boolean online) {
        // Lightweight keep-alive: update lastActivity via JPQL could be added.
        log.debug("Requested updateOnlineStatus (logical) for session {} -> {}", sessionId, online);
    }

    @Transactional(readOnly = true)
    public Optional<UserSession> findByWebsocketSessionId(String websocketSessionId) {
        return sessionRepository.findByWebsocketSessionId(websocketSessionId);
    }

    @Transactional
    public void setWebsocketSessionId(UUID sessionId, String websocketSessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setWebsocketSessionId(websocketSessionId);
            session.setOnline(true);
            session.setLastActivity(OffsetDateTime.now());
            sessionRepository.save(session);
            log.debug("Set websocketSessionId {} for session {}", websocketSessionId, sessionId);
        });
    }

    @Transactional
    public void clearWebsocketSessionId(UUID sessionId) {
        try {
            sessionRepository.findById(sessionId).ifPresent(session -> {
                session.setWebsocketSessionId(null);
                session.setOnline(false);
                session.setLastActivity(OffsetDateTime.now());
                sessionRepository.save(session);
                log.debug("Cleared websocketSessionId and set offline for session {}", sessionId);
            });
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock in clearWebsocketSessionId, falling back to retry for {}: {}", sessionId, e.getMessage());
            clearWebsocketSessionIdWithRetry(sessionId);
        }
    }

    @Transactional(readOnly = true)
    public List<UserSession> findInactiveSessions(OffsetDateTime threshold) {
        return sessionRepository.findInactiveSessions(threshold);
    }

    @Transactional(readOnly = true)
    public List<UserSession> getAllActiveSessions() {
        return sessionRepository.findAllActiveSessions();
    }

    // ----------------- safe operations -----------------

    @Transactional
    public void clearWebsocketSessionIdWithPessimisticLock(UUID sessionId) {
        sessionRepository.findByIdForUpdate(sessionId).ifPresent(session -> {
            session.setWebsocketSessionId(null);
            session.setOnline(false);
            session.setLastActivity(OffsetDateTime.now());
            sessionRepository.save(session);
            log.debug("Cleared websocketSessionId (pessimistic) and set offline for session {}", sessionId);
        });
    }

    public void clearWebsocketSessionIdWithRetry(UUID sessionId) {
        int maxAttempts = 3;
        int attempt = 0;

        while (attempt < maxAttempts) {
            attempt++;
            try {
                Boolean success = transactionTemplate.execute((TransactionCallback<Boolean>) status -> {
                    Optional<UserSession> opt = sessionRepository.findById(sessionId);
                    if (opt.isPresent()) {
                        UserSession s = opt.get();
                        s.setWebsocketSessionId(null);
                        s.setOnline(false);
                        s.setLastActivity(OffsetDateTime.now());
                        sessionRepository.save(s);
                    }
                    return Boolean.TRUE;
                });

                if (Boolean.TRUE.equals(success)) {
                    log.debug("Cleared websocketSessionId with retry for session {} on attempt {}", sessionId, attempt);
                    return;
                }
            } catch (OptimisticLockingFailureException lockEx) {
                log.warn("Optimistic lock failure clearing websocketSessionId for {} (attempt {}/{}): {}", sessionId, attempt, maxAttempts, lockEx.getMessage());
                try { Thread.sleep(50L * attempt); } catch (InterruptedException ignored) {}
            } catch (Exception e) {
                log.error("Unexpected error clearing websocketSessionId for {} (attempt {}/{}): {}", sessionId, attempt, maxAttempts, e.getMessage(), e);
                try { Thread.sleep(50L * attempt); } catch (InterruptedException ignored) {}
            }
        }

        log.error("Failed to clear websocketSessionId after {} attempts for session {}", maxAttempts, sessionId);
        try {
            transactionTemplate.execute((TransactionCallback<Void>) status -> {
                sessionRepository.findById(sessionId).ifPresent(session -> {
                    session.setWebsocketSessionId(null);
                    session.setOnline(false);
                    sessionRepository.save(session);
                });
                return null;
            });
        } catch (Exception e) {
            log.warn("Fallback DB update also failed for session {}: {}", sessionId, e.getMessage());
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public boolean updateLastActivitySafe(UUID sessionId) {
        try {
            int updated = sessionRepository.updateLastActivity(sessionId, OffsetDateTime.now());
            return updated > 0;
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock when updating lastActivity for {}: {}", sessionId, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error updating lastActivity for {}: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public void revokeSessionAndTokensByJti(UUID jti) {
        sessionRepository.findByActiveJti(jti).ifPresent(session -> {
            session.setActive(false);
            session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
            session.setActiveJti(null);
            sessionRepository.save(session);

            refreshTokenRepository.findByJti(jti).ifPresent(token -> {
                token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
                refreshTokenRepository.save(token);
            });

            log.debug("Session and associated tokens revoked for jti: {}", jti);
        });
    }
        @Transactional
    public void revokeAllActiveSessionsAndTokens(UUID userId) {
        sessionRepository.revokeAllActiveSessionsForUser(userId);
        log.debug("Revoked all active sessions and tokens for user {}", userId);
    }
}
