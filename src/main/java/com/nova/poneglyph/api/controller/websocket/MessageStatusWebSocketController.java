package com.nova.poneglyph.api.controller.websocket;

import com.nova.poneglyph.domain.enums.DeliveryStatus;
import com.nova.poneglyph.service.chat.MessageStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageStatusWebSocketController {

    private final MessageStatusService messageStatusService;

    @MessageMapping("/message-status")
    public void handleMessageStatusUpdate(@Payload Map<String, Object> payload, Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                UUID userId = UUID.fromString(authentication.getName());

                // دعم تنسيقات مختلفة للحقول
                String messageIdStr = (String) payload.get("messageId");
                if (messageIdStr == null) messageIdStr = (String) payload.get("id");

                String statusStr = (String) payload.get("status");
                String conversationIdStr = (String) payload.get("conversationId");

                if (statusStr == null) {
                    log.warn("Invalid message status update payload: {}", payload);
                    return;
                }

                DeliveryStatus status;
                try {
                    status = DeliveryStatus.valueOf(statusStr.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    log.warn("Unknown delivery status received: {}", statusStr);
                    return;
                }

                switch (status) {
                    case DELIVERED:
                        if (messageIdStr != null) {
                            try {
                                UUID messageId = UUID.fromString(messageIdStr);
                                messageStatusService.markDelivered(userId, messageId);
                            } catch (IllegalArgumentException e) {
                                log.warn("Invalid messageId format: {}", messageIdStr);
                            }
                        }
                        break;

                    case READ:
                        if (conversationIdStr != null) {
                            try {
                                UUID conversationId = UUID.fromString(conversationIdStr);
                                if (messageIdStr != null) {
                                    // حالة READ لرسالة محددة
                                    UUID messageId = UUID.fromString(messageIdStr);
                                     messageStatusService.markMessageAsRead(userId, messageId);
                                    log.debug("Message-specific READ not implemented yet");
                                } else {
                                    // حالة READ للمحادثة كاملة
                                    messageStatusService.markRead(userId, conversationId);
                                }
                            } catch (IllegalArgumentException e) {
                                log.warn("Invalid conversationId format: {}", conversationIdStr);
                            }
                        }
                        break;

                    default:
                        log.warn("Unhandled message status: {}", status);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process message status update: {}", e.getMessage(), e);
        }
    }

}
