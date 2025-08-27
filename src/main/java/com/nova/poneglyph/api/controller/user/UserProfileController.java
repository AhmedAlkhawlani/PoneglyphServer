package com.nova.poneglyph.api.controller.user;

import com.nova.poneglyph.config.v2.CustomUserDetails;
import com.nova.poneglyph.dto.userDto.UserProfileDto;
import com.nova.poneglyph.exception.AuthorizationException;
import com.nova.poneglyph.service.user.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PutMapping
    public ResponseEntity<UserProfileDto> updateProfile(
            @RequestBody UserProfileDto profileDto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId();
        UserProfileDto updatedProfile = userProfileService.updateProfile(userId, profileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/image")
    public ResponseEntity<String> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId();
        String imageUrl = userProfileService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(imageUrl);
    }

    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId();
        UserProfileDto profile = userProfileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }
}
