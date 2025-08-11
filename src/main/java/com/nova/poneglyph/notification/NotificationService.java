//package com.nova.poneglyph.notification;
//
//import com.nova.poneglyph.message.MessageResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class NotificationService {
//    private final SimpMessagingTemplate messagingTemplate;
//
//    public void sendToUser(String userName, Notification notification) {
//        log.info("Sending private notification to user {}: {}", userName, notification);
//        // 1. Notification for specific user (private queue)
//        messagingTemplate.convertAndSendToUser(
//                userName,
//                "/queue/notifications",
//                notification
//        );
//    }
//
////    public void sendToChatTopic(String chatId, Notification notification) {
////        log.info("Broadcasting notification to chat {}: {}", chatId, notification);
////        // 2. Broadcast to all chat participants
////        messagingTemplate.convertAndSend(
////                "/topic/chat/" + chatId,
////                notification
////        );
////    }
//    public void sendToChatTopic(String chatId, MessageResponse notification) {
//        log.info("Broadcasting notification to chat {}: {}", chatId, notification);
//        // 2. Broadcast to all chat participants
//        messagingTemplate.convertAndSend(
//                "/topic/chat/" + chatId,
//                notification
//        );
//    }
//
//    public void sendGlobalNotification(Notification notification) {
//        log.info("Sending global notification: {}", notification);
//        // 3. Broadcast to all connected clients
//        messagingTemplate.convertAndSend(
//                "/topic/global",
//                notification
//        );
//    }
//}
////
////@Service
////@RequiredArgsConstructor
////@Slf4j
////public class NotificationService {
////
////    private final SimpMessagingTemplate messagingTemplate;
////
////    public void sendNotification(String userId, Notification notification) {
////        log.info("Sending WS notification to {} with payload {}", userId, notification);
////        messagingTemplate.convertAndSendToUser(
////                userId,
////                "/chat",
////                notification
////        );
////    }
////    public void sendNotification_V2(String userId, Notification notification) {
////        log.info("Sending WS notification to {} with payload {}", userId, notification);
////        messagingTemplate.convertAndSend("/topic/chat/"+userId, notification);
////
//////        messagingTemplate.convertAndSendToUser(
//////                userId,
//////                "/chat",
//////                notification
//////        );
////    }
////}
