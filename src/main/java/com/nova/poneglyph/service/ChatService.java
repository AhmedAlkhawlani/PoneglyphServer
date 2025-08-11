package com.nova.poneglyph.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.poneglyph.dto.ChatMessage;
import com.nova.poneglyph.dto.Notification;
import com.nova.poneglyph.model.Message;
import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

//    @KafkaListener(
//            topics = "notifications-topic",
//            groupId = "notification-group",
//            containerFactory = "notificationKafkaListenerContainerFactory"
//    )
    public void sendMessageNotification(ChatMessage message) {
        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(message.getReceiverId())
                .message("New message from " + message.getSenderId())
                .timestamp(LocalDateTime.now())
                .type(Notification.NotificationType.MESSAGE)
                .relatedMessageId(message.getMessageId())
                .build();

        kafkaTemplate.send("notifications-topic", message.getReceiverId(), notification);
    }

    public void markMessageAsDelivered(String messageId) {
        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .relatedMessageId(messageId)
                .timestamp(LocalDateTime.now())
                .type(Notification.NotificationType.DELIVERY)
                .build();

        kafkaTemplate.send("notifications-topic", "delivery", notification);
    }

    public void markMessageAsSeen(String messageId) {
        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .relatedMessageId(messageId)
                .timestamp(LocalDateTime.now())
                .type(Notification.NotificationType.SEEN)
                .build();

        kafkaTemplate.send("notifications-topic", "seen", notification);
    }



    public void handleNotification(Notification event) {
        messagingTemplate.convertAndSendToUser(
                event.getUserId(),
                "/queue/notifications",
                event
        );
    }

    public void sendMessageNotificationFromMessage(Notification message) {
//        Notification notification = Notification.builder()
//                .notificationId(UUID.randomUUID().toString())
//                .userId(message.getConversation().getId())  // أو userId المناسب هنا — عادة هو receiver phone
//                .message("New message from " + message.getSenderPhone())
//                .timestamp(System.currentTimeMillis())
//                .type(Notification.NotificationType.MESSAGE)
//                .relatedMessageId(message.getId())
//                .build();

        kafkaTemplate.send("notifications-topic", message.getUserId(), message);
    }
    public void sendMessageNotificationFromMessage2(Message message) {
        // جلب رقم الطرف الآخر (المستلم)
        String receiverPhone = message.getConversation().getOtherParticipant(message.getSenderPhone());

        if (receiverPhone == null) {
            // هذا يعني محادثة جماعية أو مشكلة، يمكنك هنا التعامل حسب حاجتك
            receiverPhone = "unknown";
        }

        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(receiverPhone)  // ترسل للمستلم فعلياً
                .message("New message from " + message.getSenderPhone())
                .timestamp(LocalDateTime.now())
                .type(Notification.NotificationType.MESSAGE)
                .relatedMessageId(message.getId())
                .build();

        kafkaTemplate.send("notifications-topic", receiverPhone, notification);
    }


}
