
package com.nova.poneglyph.service;

import com.nova.poneglyph.config.websocket.WebSocketController;
import com.nova.poneglyph.domain.message.Call;
import com.nova.poneglyph.dto.NotificationDto;
import com.nova.poneglyph.dto.chatDto.MessageDto;
import com.nova.poneglyph.dto.conversation.MessageDTO;
import com.nova.poneglyph.dto.conversation.ConversationDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final Logger log = LoggerFactory.getLogger(WebSocketService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final Executor asyncExecutor = Executors.newFixedThreadPool(10);

    // إرسال رسالة heartbeat مع معالجة الأخطاء
//    public void sendHeartbeat(String userId) {
//        CompletableFuture.runAsync(() -> {
//            try {
//                messagingTemplate.convertAndSendToUser(
//                        userId,
//                        "/queue/heartbeat",
//                        Map.of(
//                                "type", "heartbeat",
//                                "timestamp", System.currentTimeMillis(),
//                                "userId", userId
//                        )
//                );
//            } catch (Exception e) {
//                log.warn("Failed to send heartbeat to user {}: {}", userId, e.getMessage());
//            }
//        }, asyncExecutor);
//    }

    // تحسين أداء إرسال الرسائل
    public void notifyNewMessage(UUID conversationId, MessageDto message) {
        CompletableFuture.runAsync(() -> {
            try {
                messagingTemplate.convertAndSend(
                        "/topic/conversation." + conversationId,
                        message
                );
            } catch (Exception e) {
                log.error("Failed to send message to conversation {}: {}", conversationId, e.getMessage());
            }
        }, asyncExecutor);
    }
    // إرسال رسالة heartbeat
//    public void sendHeartbeat( String userId) {
//        messagingTemplate.convertAndSendToUser(
//                userId,
//                "/queue/heartbeat",
//                Map.of(
//                        "type", "heartbeat",
//                        "timestamp", System.currentTimeMillis(),
//                        "userId", userId.toString()
//                )
//        );
//    }

    // باقي الطرق كما هي مع بعض التحسينات
//    public void notifyNewMessage(UUID conversationId, MessageDto message) {
//        messagingTemplate.convertAndSend(
//                "/topic/conversation." + conversationId,
//                message
//        );
//    }

    public void notifyNewMessage(UUID conversationId, MessageDTO message) {
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId,
                message
        );
    }

    public void notifyMessageStatus(UUID userId, UUID messageId, String status) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/message-status",
                Map.of("messageId", messageId, "status", status)
        );
    }

    public void notifyIncomingCall(Call call) {
        messagingTemplate.convertAndSendToUser(
                call.getReceiver().getId().toString(),
                "/queue/call-incoming",
                call
        );
    }

    public void notifyIncomingCall(WebSocketController.CallRequest callRequest) {
        messagingTemplate.convertAndSendToUser(
                callRequest.getReceiverId().toString(),
                "/queue/call-incoming",
                callRequest
        );
    }

    public void notifyCallStatus(UUID callId, String status) {
        messagingTemplate.convertAndSend(
                "/topic/call." + callId,
                Map.of("callId", callId, "status", status)
        );
    }

    public void notifyMessageDeleted(UUID messageId, boolean forEveryone) {
        messagingTemplate.convertAndSend(
                "/topic/message-deleted",
                Map.of("messageId", messageId, "forEveryone", forEveryone)
        );
    }

    public void notifyReactionUpdate(UUID messageId, UUID userId, String reaction) {
        messagingTemplate.convertAndSend(
                "/topic/message." + messageId + ".reactions",
                Map.of("messageId", messageId, "userId", userId, "reaction", reaction)
        );
    }

    public void sendNotification(UUID userId, NotificationDto notification) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notification",
                notification
        );
    }

//    public void notifyPresenceChange(UUID userId, boolean online) {
//        messagingTemplate.convertAndSend(
//                "/topic/presence." + userId,
//                Map.of("userId", userId, "online", online)
//        );
//    }

    public void notifyUserStatusChange(UUID userId, String status) {
        messagingTemplate.convertAndSend(
                "/topic/user-status." + userId,
                Map.of("userId", userId, "status", status)
        );
    }

    public void notifyConversationCreated(ConversationDTO conversation) {
        if (conversation == null || conversation.getId() == null) return;
        messagingTemplate.convertAndSend(
                "/topic/conversations.created",
                conversation
        );
    }

    public void notifyConversationUpdated(UUID conversationId, ConversationDTO conversation) {
        if (conversationId == null) return;
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId + ".meta",
                conversation
        );
    }

    // إرسال إشعار للمشرفين
    public void sendNotificationToAdmins(String message) {
        messagingTemplate.convertAndSend(
                "/topic/admin/notifications",
                Map.of("message", message, "timestamp", System.currentTimeMillis())
        );
    }
    /**
     * إرسال heartbeat فوري لمستخدم محدد
     */
    public void sendHeartbeat(String userId) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/heartbeat",
                    Map.of(
                            "type", "heartbeat",
                            "timestamp", System.currentTimeMillis(),
                            "userId", userId
                    )
            );
            log.debug("Heartbeat sent to user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to send heartbeat to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * إشعار حالة حضور (presence) فوري
     */
    public void notifyPresenceChange(UUID userId, boolean online) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/presence." + userId,
                    Map.of("userId", userId, "online", online)
            );
            log.debug("notifyPresenceChange sent for {} -> online={}", userId, online);
        } catch (Exception e) {
            log.warn("Failed to notify presence change for {}: {}", userId, e.getMessage(), e);
        }
    }

}
