package com.nova.poneglyph.service.user;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.NotificationSettings;
import com.nova.poneglyph.dto.PrivacySettings;

import com.nova.poneglyph.dto.userDto.UserSettingsDto;
import com.nova.poneglyph.exception.UserException;
import com.nova.poneglyph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserRepository userRepository;

    @Transactional
    public UserSettingsDto updateSettings(UUID userId, UserSettingsDto settingsDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        // تحديث إعدادات الخصوصية
        PrivacySettings privacySettings = user.getPrivacySettingsObject();
        if (privacySettings == null) {
            privacySettings = new PrivacySettings();
        }
        user.setPrivacySettingsObject(privacySettings);

        // تحديث إعدادات الإشعارات
        NotificationSettings notificationSettings = user.getNotificationSettingsObject();
        if (notificationSettings == null) {
            notificationSettings = new NotificationSettings();
        }
        user.setNotificationSettingsObject(notificationSettings);

        userRepository.save(user);
        return settingsDto;
    }

    @Transactional(readOnly = true)
    public UserSettingsDto getSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        UserSettingsDto dto = new UserSettingsDto();
        // تعيين القيم من User إلى UserSettingsDto
        return dto;
    }
}
