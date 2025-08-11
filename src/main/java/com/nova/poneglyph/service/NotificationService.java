//package com.nova.poneglyph.service;
//
//import com.nova.poneglyph.dto.Notification;
//import lombok.RequiredArgsConstructor;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class NotificationService {
//
//    private final KafkaTemplate<String, Notification> kafkaTemplate;
//    private final SimpMessagingTemplate messagingTemplate;
//
//    public void sendNotification(Notification notification) {
//        kafkaTemplate.send("notification-events", notification.getReceiverPhone(), notification);
//    }
//
//    @KafkaListener(topics = "notification-events", groupId = "notification-group")
//    public void handleNotification(Notification event) {
//        switch (event.getType()) {
//            case "NEW_MESSAGE":
//                // Private chat
//                messagingTemplate.convertAndSendToUser(
//                        event.getReceiverPhone(),
//                        "/queue/private",
//                        event
//                );
//                break;
//
//            case "GROUP_MESSAGE":
//                // Broadcast to group (assumes group has its own topic)
//                messagingTemplate.convertAndSend(
//                        "/topic/group/" + event.getConversationId(),
//                        event
//                );
//                break;
//
//            case "BROADCAST":
//                // Message to all users
//                messagingTemplate.convertAndSend("/topic/global", event);
//                break;
//        }
//    }
//}
package com.nova.poneglyph.service;

import com.nova.poneglyph.dto.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String receiverPhone, String content, Notification.NotificationType type, String relatedMessageId) {
        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(receiverPhone)
                .message(content)
                .type(type)
                .timestamp(LocalDateTime.now())
                .relatedMessageId(relatedMessageId)
                .build();

        // إرسال إلى Kafka
        kafkaTemplate.send("notification-events", receiverPhone, notification);
    }

//    @KafkaListener(topics = "notification-events", groupId = "notification-group")
    public void handleNotification(Notification event) {
        messagingTemplate.convertAndSendToUser(
                event.getUserId(),  // رقم الهاتف كمفتاح للجلسة
                "/queue/notifications",
                event
        );
    }
}
