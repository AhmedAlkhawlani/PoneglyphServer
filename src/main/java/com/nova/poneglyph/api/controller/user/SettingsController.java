package com.nova.poneglyph.api.controller.user;

import com.nova.poneglyph.config.v2.CustomUserDetails;
import com.nova.poneglyph.dto.NotificationSettings;
import com.nova.poneglyph.dto.PrivacySettings;
import com.nova.poneglyph.exception.AuthorizationException;
import com.nova.poneglyph.service.user.NotificationSettingsService;
import com.nova.poneglyph.service.user.PrivacySettingsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SettingsController {

    private final PrivacySettingsService privacySettingsService;
    private final NotificationSettingsService notificationSettingsService;

    // Privacy Settings Endpoints
    @PutMapping("/privacy")
    public ResponseEntity<PrivacySettings> updatePrivacySettings(
            @RequestBody PrivacySettings privacySettings,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        PrivacySettings updatedSettings = privacySettingsService.updatePrivacySettings(userId, privacySettings);
        return ResponseEntity.ok(updatedSettings);
    }

    @GetMapping("/privacy")
    public ResponseEntity<PrivacySettings> getPrivacySettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        PrivacySettings settings = privacySettingsService.getPrivacySettings(userId);
        return ResponseEntity.ok(settings);
    }

    // Notification Settings Endpoints
    @PutMapping("/notifications")
    public ResponseEntity<NotificationSettings> updateNotificationSettings(
            @RequestBody NotificationSettings notificationSettings,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        NotificationSettings updatedSettings = notificationSettingsService.updateNotificationSettings(userId, notificationSettings);
        return ResponseEntity.ok(updatedSettings);
    }

    @GetMapping("/notifications")
    public ResponseEntity<NotificationSettings> getNotificationSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        NotificationSettings settings = notificationSettingsService.getNotificationSettings(userId);
        return ResponseEntity.ok(settings);
    }
}
