//package com.nova.poneglyph.events;
//
//import com.nova.poneglyph.events.TypingMessage;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//import java.security.Principal;
//import java.util.UUID;
//
//@Controller
//public class TypingIndicatorController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//
//    public TypingIndicatorController(SimpMessagingTemplate messagingTemplate) {
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    @MessageMapping("/typing")
//    public void handleTyping(TypingMessage typingMessage, Principal principal) {
//        try {
//            // إذا لم يرسل العميل userId، نضيفه من الـ principal
//            if (typingMessage.getUserId() == null) {
//                typingMessage.setUserId(UUID.fromString(principal.getName()));
//            }
//
//            // تسجيل الرسالة للتصحيح
//            System.out.println("Received typing message: " + typingMessage.toString());
//
//            // بث الرسالة للجميع في المحادثة
//            messagingTemplate.convertAndSend(
//                    "/topic/conversation." + typingMessage.getConversationId() + ".typing",
//                    typingMessage
//            );
//
//            // أيضًا إرسال إلى الموضوع العام لل typing
//            messagingTemplate.convertAndSend(
//                    "/topic/typing",
//                    typingMessage
//            );
//
//        } catch (Exception e) {
//            System.err.println("Error handling typing message: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}
