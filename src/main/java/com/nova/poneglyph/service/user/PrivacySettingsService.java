package com.nova.poneglyph.service.user;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.NotificationSettings;
import com.nova.poneglyph.dto.PrivacySettings;
import com.nova.poneglyph.exception.UserException;
import com.nova.poneglyph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrivacySettingsService {

    private final UserRepository userRepository;

    @Transactional
    public PrivacySettings updatePrivacySettings(UUID userId, PrivacySettings privacySettings) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        user.setPrivacySettingsObject(privacySettings);
        userRepository.save(user);

        return privacySettings;
    }

    @Transactional(readOnly = true)
    public PrivacySettings getPrivacySettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        PrivacySettings privacySettings = user.getPrivacySettingsObject();
        if (privacySettings == null) {
            // إرجاع إعدادات افتراضية إذا لم تكن موجودة
            privacySettings = new PrivacySettings();
            privacySettings.setLastSeenVisible(true);
            privacySettings.setProfilePhotoVisible(true);
            privacySettings.setStatusVisible(true);
        }

        return privacySettings;
    }
}
