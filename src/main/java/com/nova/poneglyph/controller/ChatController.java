//package com.nova.poneglyph.controller;
//
//import com.nova.poneglyph.dto.ChatMessage;
//import com.nova.poneglyph.service.ChatService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//import java.util.UUID;
//
//@RestController
//@RequiredArgsConstructor
//public class ChatController {
//    private final SimpMessagingTemplate messagingTemplate;
////    private final SimpMessagingTemplate messagingTemplate;
//    private final ChatService chatService;
//
//    @MessageMapping("/chat/send")
//    public void sendMessage(@Payload ChatMessage message) {
//        // تعيين ID للحالة
//        message.setMessageId(UUID.randomUUID().toString());
//        message.setStatus(ChatMessage.MessageStatus.SENT);
//        message.setTimestamp(System.currentTimeMillis());
//
//        // إرسال الرسالة للمستلم عبر WebSocket
//        messagingTemplate.convertAndSendToUser(
//                message.getReceiverId(),
//                "/queue/messages",
//                message);
//
//        // إرسال إشعار عبر Kafka
//        chatService.sendMessageNotification(message);
//
//        // إرسال تأكيد للمرسل
//        messagingTemplate.convertAndSendToUser(
//                message.getSenderId(),
//                "/queue/confirmations",
//                Map.of(
//                        "type", "SENT",
//                        "messageId", message.getMessageId(),
//                        "timestamp", System.currentTimeMillis()
//                ));
//    }
//
//    @MessageMapping("/chat/markAsDelivered")
//    public void markAsDelivered(@Payload String messageId) {
//        chatService.markMessageAsDelivered(messageId);
//    }
//
//    @MessageMapping("/chat/markAsSeen")
//    public void markAsSeen(@Payload String messageId) {
//        chatService.markMessageAsSeen(messageId);
//    }
//}
