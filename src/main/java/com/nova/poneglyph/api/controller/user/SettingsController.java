//package com.nova.poneglyph.api.controller.user;
//
//
//
//import com.nova.poneglyph.service.user.SettingsService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/settings")
//@RequiredArgsConstructor
//public class SettingsController {
//
//    private final SettingsService settingsService;
//
//    @PutMapping
//    public ResponseEntity<UserSettings> updateSettings(
//            @RequestBody SettingsUpdateDto dto,
//            @AuthenticationPrincipal UUID userId) {
//        return ResponseEntity.ok(settingsService.updateSettings(userId, dto));
//    }
//
//    @GetMapping
//    public ResponseEntity<UserSettings> getSettings(
//            @AuthenticationPrincipal UUID userId) {
//        return ResponseEntity.ok(settingsService.getSettings(userId));
//    }
//}
