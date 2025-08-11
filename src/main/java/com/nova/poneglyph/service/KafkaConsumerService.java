package com.nova.poneglyph.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.poneglyph.dto.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.DataInput;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final SimpMessagingTemplate messagingTemplate;
//    @KafkaListener(topics = "notifications-topic", groupId = "chat-group")
//    public void listen(String payload) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            Notification notification = objectMapper.readValue(payload, Notification.class);
//            System.out.println("تمت المعالجة: " + notification);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
@KafkaListener(
        topics = "notifications-topic",
        groupId = "chat-group",
        containerFactory = "kafkaListenerContainerFactory"
)

public void handleNotification(String messageJson) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // لدعم LocalDateTime إذا استخدمته
        Notification notification = mapper.readValue(messageJson, Notification.class);

        switch (notification.getType()) {
            case MESSAGE:
                messagingTemplate.convertAndSendToUser(
                        notification.getUserId(),
                        "/queue/notifications",
                        notification);
                break;
            case DELIVERY:
                messagingTemplate.convertAndSendToUser(
                        getSenderIdFromMessage(notification.getRelatedMessageId()),
                        "/queue/deliveries",
                        notification);
                break;
            case SEEN:
                messagingTemplate.convertAndSendToUser(
                        getSenderIdFromMessage(notification.getRelatedMessageId()),
                        "/queue/seen",
                        notification);
                break;
            // … حالات SYSTEM, ALERT إلخ إذا أردت
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    // هذه دالة مساعدة تحتاج لتنفيذها حسب نظامك
    private String getSenderIdFromMessage(String messageId) {
        // استعلام قاعدة البيانات أو الذاكرة المؤقتة للحصول على senderId
        return "sender-id-from-db"; // مثال
    }
}
