//package com.nova.poneglyph.service;
//
//
//
//import com.nova.poneglyph.domain.user.UserSession;
//import com.nova.poneglyph.repository.UserSessionRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.OffsetDateTime;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class SessionService {
//
//    private final UserSessionRepository sessionRepository;
//
//    @Transactional
//    public UserSession createSession(UUID userId, String deviceId, String refreshTokenHash) {
//        // Revoke all existing sessions
//        sessionRepository.findByUserId(userId).forEach(session -> {
//            session.setRevokedAt(OffsetDateTime.now());
//            sessionRepository.save(session);
//        });
//
//        UserSession session = UserSession.builder()
//                .userId(userId)
//                .deviceId(deviceId)
//                .refreshTokenHash(refreshTokenHash)
//                .issuedAt(OffsetDateTime.now())
//                .expiresAt(OffsetDateTime.now().plusDays(30))
//                .build();
//
//        return sessionRepository.save(session);
//    }
//
//    @Transactional
//    public void revokeSession(UUID sessionId) {
//        sessionRepository.findById(sessionId).ifPresent(session -> {
//            session.setRevokedAt(OffsetDateTime.now());
//            sessionRepository.save(session);
//        });
//    }
//
//    @Transactional
//    public void revokeAllSessions(UUID userId) {
//        sessionRepository.findByUserId(userId).forEach(session -> {
//            session.setRevokedAt(OffsetDateTime.now());
//            sessionRepository.save(session);
//        });
//    }
//
//    //15. تنفيذ كشف إعادة الاستخدام (Refresh Token Reuse Detection)
////    @Transactional
////    public TokenResponse rotateRefresh(String rawRefresh) {
////        String hash = passwordEncoder.encode(rawRefresh);
////        RefreshToken existing = refreshTokenRepo.findByRefreshHash(hash)
////                .orElseThrow(() -> reuseDetected()); // no record -> reuse/attack
////
////        if (existing.getRevokedAt() != null ||
////                existing.getExpiresAt().isBefore(OffsetDateTime.now())) {
////
////            // Token reuse detected - revoke all tokens
////            refreshTokenRepo.revokeAllForUser(existing.getUser().getUserId());
////            throw new TokenReuseException("Token reuse detected");
////        }
////
////        // ... rest of token rotation logic
////    }
//}
package com.nova.poneglyph.service;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserSession;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserSession createSession(UUID userId, String deviceId, String refreshTokenHash) {
        // إبطال جميع الجلسات السابقة بكفاءة (استعلام واحد)
        sessionRepository.revokeAllForUser(userId);

        // الحصول على مرجع المستخدم بدون تحميل كامل (يمكن استخدام findById للتأكد)
        User user = userRepository.getReferenceById(userId);

        UserSession session = UserSession.builder()
                .user(user)
//                .deviceId(deviceId)
                .refreshTokenHash(refreshTokenHash)
                .issuedAt(OffsetDateTime.now())
                .lastUsedAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusDays(30)) // عدّل حسب إعداداتك
                .active(true)
                .build();

        return sessionRepository.save(session);
    }

    @Transactional
    public void revokeSession(UUID sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            // استخدم دالة revoke() التي في الـ entity (آمنة وواضحة)
            session.revoke();
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void revokeAllSessions(UUID userId) {
        // استدعاء استعلام واحد لإبطال كل الجلسات
        sessionRepository.revokeAllForUser(userId);
    }

    @Transactional(readOnly = true)
    public java.util.List<UserSession> getActiveSessions(UUID userId) {
        return sessionRepository.findByUser_IdAndActiveTrue(userId);
    }
}
