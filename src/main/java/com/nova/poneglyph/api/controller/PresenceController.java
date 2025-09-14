//package com.nova.poneglyph.api.controller;
//
//import com.nova.poneglyph.service.presence.PresenceService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
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
