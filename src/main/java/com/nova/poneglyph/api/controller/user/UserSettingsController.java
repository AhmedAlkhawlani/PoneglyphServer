package com.nova.poneglyph.api.controller.user;


import com.nova.poneglyph.config.v2.CustomUserDetails;
import com.nova.poneglyph.dto.userDto.UserSettingsDto;
import com.nova.poneglyph.exception.AuthorizationException;
import com.nova.poneglyph.service.user.UserSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user/settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @PutMapping
    public ResponseEntity<UserSettingsDto> updateSettings(
            @RequestBody UserSettingsDto settingsDto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        UserSettingsDto updatedSettings = userSettingsService.updateSettings(userId, settingsDto);
        return ResponseEntity.ok(updatedSettings);
    }

    @GetMapping
    public ResponseEntity<UserSettingsDto> getSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        UserSettingsDto settings = userSettingsService.getSettings(userId);
        return ResponseEntity.ok(settings);
    }
}
