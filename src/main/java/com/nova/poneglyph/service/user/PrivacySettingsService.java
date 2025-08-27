package com.nova.poneglyph.service.user;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.PrivacySettings;
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
public class PrivacySettingsService {

    private final Logger log = LoggerFactory.getLogger(PrivacySettingsService.class);
    private final UserRepository userRepository;


    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public PrivacySettings updatePrivacySettings(UUID userId, PrivacySettings privacySettings) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        // Get the current version for manual checking
        Long currentVersion = user.getVersion();

        user.setPrivacySettingsObject(privacySettings);

        // Save and check if version matches
        try {
            userRepository.save(user);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure for user {}: expected version {}", userId, currentVersion);
            throw e;
        }

        return privacySettings;
    }

    @Transactional(readOnly = true)
    public PrivacySettings getPrivacySettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        PrivacySettings privacySettings = user.getPrivacySettingsObject();
        if (privacySettings == null) {
            privacySettings = new PrivacySettings();
            privacySettings.setLastSeenVisible(true);
            privacySettings.setProfilePhotoVisible(true);
            privacySettings.setStatusVisible(true);
        }

        return privacySettings;
    }
}
