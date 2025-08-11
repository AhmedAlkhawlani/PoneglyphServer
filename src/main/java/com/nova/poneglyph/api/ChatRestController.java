//package com.nova.poneglyph.api;
//
//
//import com.nova.poneglyph.api.model.Message;
//import com.nova.poneglyph.api.model.MessageType;
//import com.nova.poneglyph.notification.Notification;
//import com.nova.poneglyph.notification.NotificationType;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//@RestController
//@RequestMapping("/api/chat")
//public class ChatRestController {
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//    @PostMapping("/send")
//    public ResponseEntity<String> sendMessage(@RequestBody Notification message) {
//        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
//        if (message.getType() == null) {
//            message.setType(NotificationType.MESSAGE);
//        }
//
//        // إرسال إلى غرفة مخصصة إن وجدت
////        if (message.getRoom() != null && !message.getRoom().isEmpty()) {
////            messagingTemplate.convertAndSend("/topic/room/" + message.getRoom(), message);
////        }
//        // إرسال رسالة خاصة
////        else if (message.getTo() != null && !message.getTo().isEmpty()) {
////            messagingTemplate.convertAndSendToUser(message.getTo(), "/queue/private", message);
////        }
//        // إرسال رسالة عامة
////        else {
//            messagingTemplate.convertAndSend("/topic/public", message);
////        }
//
//        return ResponseEntity.ok("تم الإرسال بنجاح");
//    }
//}
//
