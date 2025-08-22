//package com.nova.poneglyph.api.controller.security;
//
//
//import com.nova.poneglyph.dto.KeyRecoveryDto;
//import com.nova.poneglyph.service.security.KeyRecoveryService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/security/recovery")
//@RequiredArgsConstructor
//public class KeyRecoveryController {
//
//    private final KeyRecoveryService keyRecoveryService;
//
//    @PostMapping("/backup")
//    public ResponseEntity<Void> backupKeys(
//            @RequestBody KeyRecoveryDto dto,
//            @AuthenticationPrincipal UUID userId) {
//        keyRecoveryService.backupKeys(userId, dto.getPrivateKey(), dto.getRecoveryPassword());
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/restore")
//    public ResponseEntity<String> restoreKeys(
//            @RequestBody KeyRecoveryDto dto,
//            @AuthenticationPrincipal UUID userId) {
//        return ResponseEntity.ok(keyRecoveryService.restoreKeys(userId, dto.getRecoveryPassword()));
//    }
//}
