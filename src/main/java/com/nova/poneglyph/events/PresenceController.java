//package com.nova.poneglyph.events;
//
//
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//import java.security.Principal;
//import java.util.UUID;
//
//@Controller
//public class PresenceController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//
//    public PresenceController(SimpMessagingTemplate messagingTemplate) {
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    @MessageMapping("/presence")
//    public void handlePresence(PresenceMessage presenceMessage, Principal principal) {
//        try {
//            if (presenceMessage.getUserId() == null) {
//                presenceMessage.setUserId(UUID.fromString(principal.getName()));
//            }
//
//            // بث حدث الحضور
//            messagingTemplate.convertAndSend(
//                    "/topic/presence." + presenceMessage.getUserId(),
//                    presenceMessage
//            );
//
//            // أيضًا إرسال إلى الموضوع العام للحضور
//            messagingTemplate.convertAndSend(
//                    "/topic/presence",
//                    presenceMessage
//            );
//
//        } catch (Exception e) {
//            System.err.println("Error handling presence message: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}
//
//
