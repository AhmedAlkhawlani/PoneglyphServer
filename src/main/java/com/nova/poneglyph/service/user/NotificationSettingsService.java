package com.nova.poneglyph.service.user;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.NotificationSettings;
import com.nova.poneglyph.exception.UserException;
import com.nova.poneglyph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationSettingsService {

    private final Logger log = LoggerFactory.getLogger(NotificationSettingsService.class);
    private final UserRepository userRepository;

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public NotificationSettings updateNotificationSettings(UUID userId, NotificationSettings notificationSettings) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        // Get the current version for manual checking
        Long currentVersion = user.getVersion();

        user.setNotificationSettingsObject(notificationSettings);

        // Save and check if version matches
        try {
            userRepository.save(user);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure for user {}: expected version {}", userId, currentVersion);
            throw e;
        }



        return notificationSettings;
    }

    @Transactional(readOnly = true)
    public NotificationSettings getNotificationSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        NotificationSettings notificationSettings = user.getNotificationSettingsObject();
        if (notificationSettings == null) {
            notificationSettings = new NotificationSettings();
            notificationSettings.setCallEnabled(true);
            notificationSettings.setMessageEnabled(true);
            notificationSettings.setMentionEnabled(true);
        }

        return notificationSettings;
    }
}
