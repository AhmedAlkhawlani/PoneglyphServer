package com.nova.poneglyph.controller;

import com.nova.poneglyph.dto.MessageDTO;
import com.nova.poneglyph.dto.PresenceDTO;
import com.nova.poneglyph.dto.SendMessageRequest;
import com.nova.poneglyph.events.ConversationUpdateEvent;
import com.nova.poneglyph.events.NotificationEvent;
import com.nova.poneglyph.notification.NotificationService;
import com.nova.poneglyph.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test-notifications")
@RequiredArgsConstructor
public class TestNotificationController {

    private final NotificationService notificationService;
    private final MessageService messageService;
    // -------------------- إرسال رسالة عادية أو Reply --------------------
    @PostMapping("/message")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO messageDTO) {
        if (messageDTO.getSentAt() == null) {
            messageDTO.setSentAt(LocalDateTime.now());
        }
        notificationService.sendMessageNotification(
                messageDTO.getConversationId(), // يمكن أن تستخدم رقم المستلم هنا
                messageDTO
        );
        return ResponseEntity.ok("Message sent successfully!");
    }

    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestBody @Valid SendMessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
    }


    // -------------------- إرسال تنبيه ⚠️ أو System --------------------
    @PostMapping("/system-alert")
    public ResponseEntity<String> sendSystemAlert(
            @RequestParam String receiverId,
            @RequestParam String message,
            @RequestParam(defaultValue = "SYSTEM") String type // SYSTEM أو ALERT
    ) {
        NotificationEvent.EventType eventType = "ALERT".equalsIgnoreCase(type) ?
                NotificationEvent.EventType.ALERT : NotificationEvent.EventType.SYSTEM;

        notificationService.sendSystemAlert(receiverId, message, eventType);

        return ResponseEntity.ok("System alert sent successfully!");
    }

    // -------------------- إرسال تحديث محادثة --------------------
    @PostMapping("/conversation-update")
    public ResponseEntity<String> sendConversationUpdate(
            @RequestParam String participantId,
            @RequestBody ConversationUpdateEvent updateEvent
    ) {
        notificationService.sendConversationUpdate(participantId, updateEvent);
        return ResponseEntity.ok("Conversation update sent successfully!");
    }

    // -------------------- إرسال Presence / Typing --------------------
    @PostMapping("/presence")
    public ResponseEntity<String> sendPresence(
            @RequestParam String receiverId,
            @RequestBody PresenceDTO presenceDTO
    ) {
        notificationService.sendPresenceUpdate(receiverId, presenceDTO);
        return ResponseEntity.ok("Presence update sent successfully!");
    }

    @PostMapping("/typing")
    public ResponseEntity<String> sendTyping(
            @RequestParam String receiverId,
            @RequestBody PresenceDTO typingDTO
    ) {
        notificationService.sendTypingEvent(receiverId, typingDTO);
        return ResponseEntity.ok("Typing event sent successfully!");
    }
}
