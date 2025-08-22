//package com.nova.poneglyph.service.user;
//
//
//
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.domain.user.UserSettings;
//import com.nova.poneglyph.dto.SettingsUpdateDto;
//import com.nova.poneglyph.exception.UserException;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.repository.UserSettingsRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class SettingsService {
//
//    private final UserSettingsRepository settingsRepository;
//    private final UserRepository userRepository;
//
//    @Transactional
//    public UserSettings updateSettings(UUID userId, SettingsUpdateDto dto) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UserException("User not found"));
//
//        UserSettings settings = user.getSettings();
//        if (settings == null) {
//            settings = new UserSettings();
//            settings.setUser(user);
//        }
//
//        settings.setMessageNotifications(dto.isMessageNotifications());
//        settings.setCallNotifications(dto.isCallNotifications());
//        settings.setGroupNotifications(dto.isGroupNotifications());
//        settings.setOnlineStatusVisible(dto.isOnlineStatusVisible());
//        settings.setReadReceipts(dto.isReadReceipts());
//        settings.setTheme(dto.getTheme());
//        settings.setLanguage(dto.getLanguage());
//
//        return settingsRepository.save(settings);
//    }
//
//    @Transactional(readOnly = true)
//    public UserSettings getSettings(UUID userId) {
//        return settingsRepository.findByUserId(userId)
//                .orElseThrow(() -> new UserException("Settings not found"));
//    }
//}
