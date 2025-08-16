package com.nova.poneglyph.notification;

import com.nova.poneglyph.dto.*;
import com.nova.poneglyph.events.ConversationUpdateEvent;
import com.nova.poneglyph.events.NotificationEvent;
import com.nova.poneglyph.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/*---------    v2.  --------*/

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;

    private static final String NOTIFICATIONS_TOPIC = "notifications-topic";

    public void sendMessageNotification(String receiverId, MessageDTO messageDTO) {
        NotificationEvent event = buildNotificationEvent(receiverId, NotificationEvent.EventType.MESSAGE)
                .message(messageDTO)
                .relatedMessageId(messageDTO.getId())
                .build();

        sendNotification(event, "/queue/messages", messageDTO);
    }

    public void sendConversationUpdate(String participantId, ConversationUpdateEvent updateEvent) {
        NotificationEvent event = buildNotificationEvent(participantId, NotificationEvent.EventType.CONVERSATION_UPDATE)
                .conversationUpdate(updateEvent)
                .build();

        sendNotification(event, "/queue/conversation-updates", updateEvent);
    }

    public void sendPresenceUpdate(String receiverId, PresenceDTO presenceDTO) {
        NotificationEvent event = buildNotificationEvent(receiverId, NotificationEvent.EventType.PRESENCE)
                .presence(presenceDTO)
                .build();

        sendNotification(event, "/queue/presence", presenceDTO);
    }

    public void sendSystemAlert(String receiverId, String systemMessage, NotificationEvent.EventType type) {
        validateAlertType(type);

        NotificationEvent event = buildNotificationEvent(receiverId, type)
                .systemMessage(systemMessage)
                .build();

        sendNotification(event, "/queue/alerts", event);
//        sendNotification(event, "/queue/alerts", systemMessage + " [Kafka]");
    }

    public void sendTypingEvent(String receiverId, PresenceDTO typingDTO) {
        NotificationEvent event = buildNotificationEvent(receiverId, NotificationEvent.EventType.TYPING)
                .presence(typingDTO)
                .build();

        sendNotification(event, "/queue/typing", typingDTO);
    }

    private NotificationEvent.NotificationEventBuilder buildNotificationEvent(String userId, NotificationEvent.EventType type) {
        return NotificationEvent.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(userId)
                .eventType(type)
                .timestamp(LocalDateTime.now());
    }

    private void sendNotification(NotificationEvent event, String destination, Object payload) {
        kafkaTemplate.send(NOTIFICATIONS_TOPIC, event.getUserId(), event);

        // إرسال مباشر فقط إذا كان متصلاً ولم يتم إرساله سابقاً
        if (presenceService.isUserConnected(event.getUserId())) {
            messagingTemplate.convertAndSendToUser(event.getUserId(), destination, payload);
        }

    }

    private void validateAlertType(NotificationEvent.EventType type) {
        if (type != NotificationEvent.EventType.SYSTEM && type != NotificationEvent.EventType.ALERT) {
            throw new IllegalArgumentException("Type must be SYSTEM or ALERT");
        }
    }
}
