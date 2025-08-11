//package com.nova.poneglyph.controller;
//
//import com.nova.poneglyph.dto.Notification;
//import com.nova.poneglyph.service.NotificationService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/v1/chat-service/notifications")
//@RequiredArgsConstructor
//public class NotificationController {
//    private final KafkaProducer kafkaProducer;
//
//
//    @PostMapping("/send")
//    public ResponseEntity<String> sendNotification(@RequestBody NotificationDto notificationDto) {
//        kafkaProducer.sendNotification(notificationDto);
//        return ResponseEntity.ok("Notification sent to Kafka topic successfully.");
//    }
//    private final NotificationService notificationService;
//
//    // إرسال إشعار عام (مثلاً system message)
//    @PostMapping("/system")
//    public String sendSystemNotification(@RequestParam String toPhone, @RequestParam String message) {
//        notificationService.sendNotification(toPhone, message, Notification.NotificationType.SYSTEM, null);
//        return "System notification sent to " + toPhone;
//    }
//
//    // إرسال إشعار رسالة خاصة
//    @PostMapping("/private")
//    public String sendPrivateMessageNotification(@RequestParam String toPhone,
//                                                 @RequestParam String fromPhone,
//                                                 @RequestParam String message,
//                                                 @RequestParam(required = false) String messageId) {
//        String content = "رسالة جديدة من " + fromPhone + ": " + message;
//        notificationService.sendNotification(toPhone, content, Notification.NotificationType.MESSAGE, messageId);
//        return "Private message notification sent.";
//    }
//
//    // إشعار لمجموعة (ترسل إلى كل عضو في المجموعة)
//    @PostMapping("/group")
//    public String sendGroupMessageNotification(@RequestParam String[] groupMembersPhones,
//                                               @RequestParam String fromPhone,
//                                               @RequestParam String groupName,
//                                               @RequestParam String message,
//                                               @RequestParam(required = false) String messageId) {
//        for (String memberPhone : groupMembersPhones) {
//            String content = "رسالة جديدة في المجموعة " + groupName + " من " + fromPhone + ": " + message;
//            notificationService.sendNotification(memberPhone, content, Notification.NotificationType.MESSAGE, messageId);
//        }
//        return "Group message notifications sent.";
//    }
//}
