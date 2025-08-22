//package com.nova.poneglyph.api.controller;
//
//
//
//import com.nova.poneglyph.domain.message.Call;
//import com.nova.poneglyph.dto.CallDto;
//import com.nova.poneglyph.dto.CallInitiateDto;
//
//import com.nova.poneglyph.service.call.CallService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/calls")
//@RequiredArgsConstructor
//public class CallController {
//
//    private final CallService callService;
//
//    @PostMapping
//    public ResponseEntity<CallDto> initiateCall(
//            @RequestBody CallInitiateDto dto,
//            @AuthenticationPrincipal UUID callerId) {
//        return ResponseEntity.ok(callService.initiateCall(dto, callerId));
//    }
//
//    @PostMapping("/{callId}/status")
//    public ResponseEntity<Void> updateCallStatus(
//            @PathVariable UUID callId,
//            @RequestParam String status) {
//        callService.updateCallStatus(callId, status);
//        return ResponseEntity.ok().build();
//    }
//}
