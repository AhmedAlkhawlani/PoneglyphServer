//package com.nova.poneglyph.service;
//
//import com.nova.poneglyph.config.websocket.WebSocketController;
//import com.nova.poneglyph.domain.message.Call;
//import com.nova.poneglyph.dto.NotificationDto;
//// قد توجد لديك Message DTOs في حزم مختلفة؛ نحتفظ بالتوافق مع النوعين
//import com.nova.poneglyph.dto.chatDto.MessageDto; // للتماشي مع أي جزء قد يستخدم هذا النوع
//import com.nova.poneglyph.dto.conversation.MessageDTO; // النوع المستخدم في الـ Conversations API
//import com.nova.poneglyph.dto.conversation.ConversationDTO;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class WebSocketService {
//
//    private final SimpMessagingTemplate messagingTemplate;
//
//    // موجود سابقًا — نحتفظ به للتوافق مع كود قديم
//    public void notifyNewMessage(UUID conversationId, MessageDto message) {
//        messagingTemplate.convertAndSend(
//                "/topic/conversation." + conversationId,
//                message
//        );
//    }
//
//    // Overload: اقبل MessageDTO من حزمة conversations (التي نستخدمها في API)
//    public void notifyNewMessage(UUID conversationId, MessageDTO message) {
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
//                Map.of("messageId", messageId, "status", status)
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
//    public void notifyIncomingCall(WebSocketController.CallRequest callRequest) {
//        messagingTemplate.convertAndSendToUser(
//                callRequest.getReceiverId().toString(),
//                "/queue/call-incoming",
//                callRequest
//        );
//    }
//
//    public void notifyCallStatus(UUID callId, String status) {
//        messagingTemplate.convertAndSend(
//                "/topic/call." + callId,
//                Map.of("callId", callId, "status", status)
//        );
//    }
//
//    public void notifyMessageDeleted(UUID messageId, boolean forEveryone) {
//        messagingTemplate.convertAndSend(
//                "/topic/message-deleted",
//                Map.of("messageId", messageId, "forEveryone", forEveryone)
//        );
//    }
//
//    public void notifyReactionUpdate(UUID messageId, UUID userId, String reaction) {
//        messagingTemplate.convertAndSend(
//                "/topic/message." + messageId + ".reactions",
//                Map.of("messageId", messageId, "userId", userId, "reaction", reaction)
//        );
//    }
//
//    public void sendNotification(UUID userId, NotificationDto notification) {
//        messagingTemplate.convertAndSendToUser(
//                userId.toString(),
//                "/queue/notification",
//                notification
//        );
//    }
//
//    public void notifyPresenceChange(UUID userId, boolean online) {
//        messagingTemplate.convertAndSend(
//                "/topic/presence." + userId,
//                Map.of("userId", userId, "online", online)
//        );
//    }
//
//    public void notifyUserStatusChange(UUID userId, String status) {
//        messagingTemplate.convertAndSend(
//                "/topic/user-status." + userId,
//                Map.of("userId", userId, "status", status)
//        );
//    }
//
//    /**
//     * إشعار عند إنشاء محادثة جديدة — يرسل DTO كامل للمحادثة
//     */
//    public void notifyConversationCreated(ConversationDTO conversation) {
//        if (conversation == null || conversation.getId() == null) return;
//        // تبليغ على topic عام ويمكن أيضًا إرسال إلى المستخدمين على queues منفردة إن أردت
//        messagingTemplate.convertAndSend(
//                "/topic/conversations.created",
//                conversation
//        );
//    }
//
//    /**
//     * إشعار عند تعديل بيانات المحادثة (مثلاً تغيير العنوان) — ترسل للمشتركين في المحادثة
//     */
//    public void notifyConversationUpdated(UUID conversationId, ConversationDTO conversation) {
//        if (conversationId == null) return;
//        messagingTemplate.convertAndSend(
//                "/topic/conversation." + conversationId + ".meta",
//                conversation
//        );
//    }
//}

package com.nova.poneglyph.service;

import com.nova.poneglyph.config.websocket.WebSocketController;
import com.nova.poneglyph.domain.message.Call;
import com.nova.poneglyph.dto.NotificationDto;
import com.nova.poneglyph.dto.chatDto.MessageDto;
import com.nova.poneglyph.dto.conversation.MessageDTO;
import com.nova.poneglyph.dto.conversation.ConversationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // إرسال رسالة heartbeat
    public void sendHeartbeat(String websocketSessionId, UUID userId) {
        messagingTemplate.convertAndSendToUser(
                websocketSessionId,
                "/queue/heartbeat",
                Map.of(
                        "type", "heartbeat",
                        "timestamp", System.currentTimeMillis(),
                        "userId", userId.toString()
                )
        );
    }

    // باقي الطرق كما هي مع بعض التحسينات
    public void notifyNewMessage(UUID conversationId, MessageDto message) {
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId,
                message
        );
    }

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

    public void notifyPresenceChange(UUID userId, boolean online) {
        messagingTemplate.convertAndSend(
                "/topic/presence." + userId,
                Map.of("userId", userId, "online", online)
        );
    }

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
}
