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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public UserSession createSession(UUID userId, UserDevice device, String refreshTokenHash, String ip) {
        // إبطال جميع الجلسات السابقة بكفاءة
        revokeAllSessions(userId);

        User user = userRepository.getReferenceById(userId);

        UserSession session = UserSession.builder()
                .user(user)
                .device(device)
                .refreshTokenHash(refreshTokenHash)
                .issuedAt(OffsetDateTime.now())
                .lastUsedAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusDays(30))
                .ipAddress(ip)
                .active(true)
                .build();

        return sessionRepository.save(session);
    }

    @Transactional
    public UserSession saveSession(UserSession session) {
        return sessionRepository.save(session);
    }

    @Transactional
    public void revokeSession(UUID sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.revoke();
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void revokeSessionByJti(UUID jti) {
        sessionRepository.findByActiveJti(jti).ifPresent(session -> {
            // إلغاء الجلسة بشكل كامل وليس فقط إزالة activeJti
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
    }

    @Transactional
    public void revokeAllActiveSessionsAndTokens(UUID userId) {
        sessionRepository.revokeAllActiveSessionsForUser(userId);
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
        });
    }

    @Transactional
    public void updateOnlineStatus(UUID sessionId, boolean online) {
        sessionRepository.updateOnlineStatus(sessionId, online);
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
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void clearWebsocketSessionId(UUID sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setWebsocketSessionId(null);
            session.setOnline(false);
            sessionRepository.save(session);
        });
    }

    @Transactional(readOnly = true)
    public List<UserSession> findInactiveSessions(OffsetDateTime threshold) {
        return sessionRepository.findInactiveSessions(threshold);
    }

    @Transactional
    public void revokeSessionAndTokensByJti(UUID jti) {
        sessionRepository.findByActiveJti(jti).ifPresent(session -> {
            // إلغاء الجلسة
            session.setActive(false);
            session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
            session.setActiveJti(null);
            sessionRepository.save(session);

            // إلغاء جميع التوكنات المرتبطة بهذه الجلسة
            refreshTokenRepository.findByJti(jti).ifPresent(token -> {
                token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
                refreshTokenRepository.save(token);
            });

            log.debug("Session and associated tokens revoked for jti: {}", jti);
        });
    }
}
