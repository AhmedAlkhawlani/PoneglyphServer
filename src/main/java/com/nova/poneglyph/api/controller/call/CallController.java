package com.nova.poneglyph.api.controller.call;





import com.nova.poneglyph.dto.callDto.CallDto;
import com.nova.poneglyph.dto.callDto.CallInitiateDto;
import com.nova.poneglyph.service.call.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final CallService callService;

    @PostMapping
    public ResponseEntity<CallDto> initiateCall(
            @RequestBody CallInitiateDto dto,
            @AuthenticationPrincipal UUID callerId) {
        return ResponseEntity.ok(callService.initiateCall(dto, callerId));
    }

    @PutMapping("/{callId}/status")
    public ResponseEntity<Void> updateCallStatus(
            @PathVariable UUID callId,
            @RequestParam String status,
            @AuthenticationPrincipal UUID userId) {
        callService.updateCallStatus(callId, status, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{callId}")
    public ResponseEntity<CallDto> getCall(
            @PathVariable UUID callId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(callService.getCall(callId, userId));
    }
}
