package com.nova.poneglyph.config.v2;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserSession;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.repository.UserSessionRepository;
import com.nova.poneglyph.service.audit.AuditService;
import com.nova.poneglyph.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuspiciousActivityMonitor {
    private static final Logger log = LoggerFactory.getLogger(SuspiciousActivityMonitor.class);

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Scheduled(fixedRate = 300000) // كل 5 دقائق
    @Transactional
    public void checkSuspiciousActivity() {
        checkMultipleSessionsFromDifferentLocations();
        checkUnusualLoginTimes();
    }

    private void checkMultipleSessionsFromDifferentLocations() {
        List<User> activeUsers = userRepository.findOnlineUsers();

        for (User user : activeUsers) {
            List<UserSession> sessions = sessionRepository.findActiveSessionsByUserId(user.getId());

            if (sessions.size() > 1) {
                boolean suspicious = sessions.stream()
                        .map(UserSession::getIpAddress)
                        .distinct()
                        .count() > 1;

                if (suspicious) {
                    notifySuspiciousActivity(user.getId(),
                            "Multiple active sessions from different locations");

                    // إلغاء الجلسات القديمة
                    sessions.stream()
                            .skip(1) // الاحتفاظ بالجلسة الأحدث فقط
                            .forEach(session -> {
                                session.revoke();
                                sessionRepository.save(session);
                            });
                }
            }
        }
    }

    private void checkUnusualLoginTimes() {
        // التحقق من أوقات تسجيل الدخول غير الاعتيادية
        OffsetDateTime now = OffsetDateTime.now();

        // تحديد فترة الليل (1 صباحًا إلى 5 صباحًا)
        OffsetDateTime nightStart = now.withHour(1).withMinute(0).withSecond(0);
        OffsetDateTime nightEnd = now.withHour(5).withMinute(0).withSecond(0);

        // إذا كنا حالياً في فترة النهار، نتحقق من الليلة الماضية
        if (now.getHour() >= 5 || now.getHour() < 1) {
            nightStart = nightStart.minusDays(1);
            nightEnd = nightEnd.minusDays(1);
        }

        List<UserSession> recentSessions = sessionRepository.findByIssuedAtBetween(nightStart, nightEnd);

        for (UserSession session : recentSessions) {
            notifySuspiciousActivity(session.getUser().getId(),
                    "Login during unusual hours (1AM-5AM) detected");
        }
    }

    private void notifySuspiciousActivity(UUID userId, String message) {
        notificationService.sendSecurityNotification(
                userId,
                "Suspicious Activity Detected",
                message
        );

        // تسجيل الحدث في سجلات التدقيق
        auditService.logSecurityEvent(
                userId,
                "SUSPICIOUS_ACTIVITY",
                message
        );
    }
}
