package com.nova.poneglyph.api.controller.chat;

import com.nova.poneglyph.config.v2.CustomUserDetails;
import com.nova.poneglyph.domain.enums.DeliveryStatus;
import com.nova.poneglyph.domain.message.Message;
import com.nova.poneglyph.repository.MessageRepository;
import com.nova.poneglyph.repository.ParticipantRepository;
import com.nova.poneglyph.service.chat.MessageStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageStatusController {

    private final MessageStatusService messageStatusService;
    private final MessageRepository messageRepository;
    private final ParticipantRepository participantRepository;

    @PostMapping("/{messageId}/status")
    public ResponseEntity<Void> updateMessageStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID messageId,
            @RequestParam String status) {

        UUID userId = userDetails.getId();

        // التحقق من أن المستخدم لديه صلاحية الوصول إلى الرسالة
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // تحقق مما إذا كان المستخدم مشاركًا في المحادثة
        boolean isParticipant = participantRepository.existsByConversationAndUser_Id(message.getConversation(), userId);
        if (!isParticipant) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            DeliveryStatus deliveryStatus = DeliveryStatus.valueOf(status.toUpperCase());

            switch (deliveryStatus) {
                case DELIVERED -> messageStatusService.markDelivered(userId, messageId);
                case READ -> messageStatusService.markRead(userId, message.getConversation().getId());
                case DELETED -> messageStatusService.markDeleted(messageId, userId, false);
                case SENT -> {
                    // عادةً لا يتم تعيين SENT من العميل
                    return ResponseEntity.badRequest().build();
                }
            }

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
