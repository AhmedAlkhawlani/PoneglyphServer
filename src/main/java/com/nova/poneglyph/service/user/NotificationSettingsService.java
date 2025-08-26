package com.nova.poneglyph.service.user;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.NotificationSettings;
import com.nova.poneglyph.exception.UserException;
import com.nova.poneglyph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationSettingsService {

    private final UserRepository userRepository;

    @Transactional
    public NotificationSettings updateNotificationSettings(UUID userId, NotificationSettings notificationSettings) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        user.setNotificationSettingsObject(notificationSettings);
        userRepository.save(user);

        return notificationSettings;
    }

    @Transactional(readOnly = true)
    public NotificationSettings getNotificationSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        NotificationSettings notificationSettings = user.getNotificationSettingsObject();
        if (notificationSettings == null) {
            // إرجاع إعدادات افتراضية إذا لم تكن موجودة
            notificationSettings = new NotificationSettings();
            notificationSettings.setCallEnabled(true);
            notificationSettings.setMessageEnabled(true);
            notificationSettings.setMentionEnabled(true);
        }

        return notificationSettings;
    }
}
