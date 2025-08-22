//package com.nova.poneglyph.config.monitor;
//
//
//
//import com.nova.poneglyph.domain.enums.AccountStatus;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.service.auth.AuthService;
//import com.nova.poneglyph.service.notification.NotificationService;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.Duration;
//import java.time.OffsetDateTime;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class SecurityMonitor {
//
//    private final UserRepository userRepository;
//    private final AuthService authService;
//    private final NotificationService notificationService;
//
//    private static final Duration INACTIVE_SESSION_THRESHOLD = Duration.ofDays(30);
//    private static final Duration PASSWORD_CHANGE_THRESHOLD = Duration.ofDays(90);
//
//    @Scheduled(cron = "0 0 3 * * ?") // Run daily at 3 AM
//    public void monitorInactiveSessions() {
//        OffsetDateTime threshold = OffsetDateTime.now().minus(INACTIVE_SESSION_THRESHOLD);
//
//        userRepository.findUsersWithInactiveSessions(threshold).forEach(user -> {
//            authService.revokeAllSessions(user.getUserId());
//            notificationService.sendSecurityNotification(
//                    user.getUserId(),
//                    "Sessions Revoked",
//                    "Your inactive sessions have been revoked for security"
//            );
//        });
//    }
//
//    @Scheduled(cron = "0 0 4 * * ?") // Run daily at 4 AM
//    public void monitorPasswordAge() {
//        OffsetDateTime threshold = OffsetDateTime.now().minus(PASSWORD_CHANGE_THRESHOLD);
//
//        userRepository.findUsersWithOldPasswords(threshold).forEach(user -> {
//            notificationService.sendSecurityNotification(
//                    user.getUserId(),
//                    "Password Update Recommended",
//                    "Your password hasn't been changed in over 90 days. Please consider updating it."
//            );
//        });
//    }
//
//    @Scheduled(fixedRate = 300000) // Every 5 minutes
//    public void monitorFailedLoginAttempts() {
//        userRepository.findUsersWithExcessiveFailedAttempts(5).forEach(user -> {
//            notificationService.sendSecurityNotification(
//                    user.getUserId(),
//                    "Suspicious Activity Detected",
//                    "Multiple failed login attempts detected on your account"
//            );
//
//            // Optionally temporarily lock account
//            if (user.getFailedLoginAttempts() > 10) {
//                user.setAccountStatus(AccountStatus.SUSPENDED);
//                userRepository.save(user);
//            }
//        });
//    }
//
//    @PostConstruct
//    public void init() {
//        // Initialize security monitoring
//    }
//}

package com.nova.poneglyph.config.monitor;

import com.nova.poneglyph.domain.enums.AccountStatus;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.service.auth.AuthService;
import com.nova.poneglyph.service.notification.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class SecurityMonitor {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    private static final Duration INACTIVE_SESSION_THRESHOLD = Duration.ofDays(30);
    private static final Duration PASSWORD_CHANGE_THRESHOLD = Duration.ofDays(90);

    @Scheduled(cron = "0 0 3 * * ?") // Run daily at 3 AM
    @Transactional
    public void monitorInactiveSessions() {
        OffsetDateTime threshold = OffsetDateTime.now().minus(INACTIVE_SESSION_THRESHOLD);

        userRepository.findUsersWithInactiveSessions(threshold).forEach(user -> {
            authService.revokeAllSessions(user.getId());
            notificationService.sendSecurityNotification(
                    user.getId(),
                    "Sessions Revoked",
                    "Your inactive sessions have been revoked for security"
            );
        });
    }

    @Scheduled(cron = "0 0 4 * * ?") // Run daily at 4 AM
    @Transactional(readOnly = true)
    public void monitorPasswordAge() {
        OffsetDateTime threshold = OffsetDateTime.now().minus(PASSWORD_CHANGE_THRESHOLD);

        userRepository.findUsersWithOldPasswords(threshold).forEach(user -> {
            notificationService.sendSecurityNotification(
                    user.getId(),
                    "Password Update Recommended",
                    "Your password hasn't been changed in over 90 days. Please consider updating it."
            );
        });
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void monitorFailedLoginAttempts() {
        userRepository.findUsersWithExcessiveFailedAttempts(5).forEach(user -> {
            notificationService.sendSecurityNotification(
                    user.getId(),
                    "Suspicious Activity Detected",
                    "Multiple failed login attempts detected on your account"
            );

            // Optionally temporarily lock account
            if (user.getFailedLoginAttempts() > 10) {
                user.setAccountStatus(AccountStatus.SUSPENDED);
                userRepository.save(user);
            }
        });
    }

    @PostConstruct
    public void init() {
        // Initialize security monitoring (any startup tasks)
    }
}
