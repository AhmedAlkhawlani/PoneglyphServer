//package com.nova.poneglyph.api.controller;
//
//import com.nova.poneglyph.dto.TypingEventRequest;
//import com.nova.poneglyph.service.PresenceService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/presence")
//@RequiredArgsConstructor
//public class PresenceController {
//    private final PresenceService presenceService;
//
//    @PostMapping("/typing")
//    public ResponseEntity<Void> sendTypingEvent(
//            @RequestBody @Valid TypingEventRequest request) {
//        presenceService.handleTypingEvent(request);
//        return ResponseEntity.ok().build();
//    }
//
//
////    @PostMapping("/{phoneNumber}/status")
////    public ResponseEntity<Void> updatePresence(
////            @PathVariable String phoneNumber,
////            @RequestParam boolean isOnline) {
////        presenceService.updateUserPresence(phoneNumber, isOnline);
////        return ResponseEntity.ok().build();
////    }
//
//}
