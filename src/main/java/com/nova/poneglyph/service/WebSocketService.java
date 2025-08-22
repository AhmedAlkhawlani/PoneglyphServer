package com.nova.poneglyph.service;



import com.nova.poneglyph.domain.message.Call;
import com.nova.poneglyph.domain.message.Message;
import com.nova.poneglyph.dto.NotificationDto;
import com.nova.poneglyph.dto.chatDto.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

//@Service
//@RequiredArgsConstructor
//public class WebSocketService {
//
//    private final SimpMessagingTemplate messagingTemplate;
//
//    public void notifyNewMessage(UUID conversationId, Message message) {
//        messagingTemplate.convertAndSend(
//                "/topic/conversation." + conversationId,
//                message
//        );
//    }
//
//    public void notifyMessageStatus(UUID userId, UUID messageId, String status) {
//        messagingTemplate.convertAndSendToUser(
//                userId.toString(),
//                "/queue/message-status",
//                new MessageStatusUpdate(messageId, status)
//        );
//    }
//
//    public void notifyIncomingCall(Call call) {
//        messagingTemplate.convertAndSendToUser(
//                call.getReceiver().getId().toString(),
//                "/queue/call-incoming",
//                call
//        );
//    }
//
//    public void notifyCallStatus(UUID callId, String status) {
//        messagingTemplate.convertAndSend(
//                "/topic/call." + callId,
//                new CallStatusUpdate(callId, status)
//        );
//    }
//
//    public record MessageStatusUpdate(UUID messageId, String status) {}
//    public record CallStatusUpdate(UUID callId, String status) {}
//
//}

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyNewMessage(UUID conversationId, MessageDto message) {
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId,
                message
        );
    }

    public void notifyMessageStatus(UUID userId, UUID messageId, String status) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/message-status",
                new MessageStatusUpdate(messageId, status)
        );
    }

    public void notifyIncomingCall(Call call) {
        messagingTemplate.convertAndSendToUser(
                call.getReceiver().getId().toString(),
                "/queue/call-incoming",
                call
        );
    }

    public void notifyCallStatus(UUID callId, String status) {
        messagingTemplate.convertAndSend(
                "/topic/call." + callId,
                new CallStatusUpdate(callId, status)
        );
    }

    public void notifyMessageDeleted(UUID messageId, boolean forEveryone) {
        messagingTemplate.convertAndSend(
                "/topic/message-deleted",
                java.util.Map.of(
                        "messageId", messageId,
                        "forEveryone", forEveryone
                )
        );
    }

    // ⚡ إضافة notifyReactionUpdate
    public void notifyReactionUpdate(UUID messageId, UUID userId, String reaction) {
        messagingTemplate.convertAndSend(
                "/topic/message." + messageId + ".reactions",
                java.util.Map.of(
                        "messageId", messageId,
                        "userId", userId,
                        "reaction", reaction
                )
        );
    }

    // 🔔 إضافة sendNotification
    public void sendNotification(UUID userId, NotificationDto notification) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notification",
                notification
        );
    }

    public void notifyPresenceChange(UUID userId, boolean online) {
        messagingTemplate.convertAndSend(
                "/topic/presence." + userId,
                java.util.Map.of(
                        "userId", userId,
                        "online", online
                )
        );
    }


    public record MessageStatusUpdate(UUID messageId, String status) {}
    public record CallStatusUpdate(UUID callId, String status) {}
}

//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class MessageStatusUpdate {
//    private UUID messageId;
//    private String status;
//}
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class CallStatusUpdate {
//    private UUID callId;
//    private String status;
//}
