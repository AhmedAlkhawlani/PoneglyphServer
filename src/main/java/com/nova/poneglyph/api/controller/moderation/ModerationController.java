package com.nova.poneglyph.api.controller.moderation;




import com.nova.poneglyph.domain.enums.BanType;
import com.nova.poneglyph.domain.enums.ReportStatus;
import com.nova.poneglyph.dto.moderationDto.ReportDto;
import com.nova.poneglyph.service.moderation.ModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    @PostMapping("/block/{userId}")
    public ResponseEntity<Void> blockUser(
            @PathVariable UUID userId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "false") boolean silent,
            @AuthenticationPrincipal UUID blockerId) {
        moderationService.blockUser(blockerId, userId, reason, silent);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unblock/{userId}")
    public ResponseEntity<Void> unblockUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UUID blockerId) {
        moderationService.unblockUser(blockerId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ban")
    public ResponseEntity<Void> banUser(
            @RequestParam String phone,
            @RequestParam String banType,
            @RequestParam String reason,
            @RequestParam(required = false) String details,
            @AuthenticationPrincipal UUID bannedBy) {
        moderationService.banUser(phone, BanType.valueOf(banType), reason, details, bannedBy);
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/report")
//    public ResponseEntity<Long> createReport(
//            @RequestBody ReportDto reportDto,
//            @AuthenticationPrincipal UUID reporterId) {
//        return ResponseEntity.ok(moderationService.createReport(reportDto, reporterId));
//    }

    @PostMapping("/report/{reportId}/resolve")
    public ResponseEntity<Void> resolveReport(
            @PathVariable Long reportId,
            @RequestParam String status,
            @RequestParam String adminNotes) {
        moderationService.resolveReport(reportId, adminNotes, ReportStatus.valueOf(status));
        return ResponseEntity.ok().build();
    }
}
