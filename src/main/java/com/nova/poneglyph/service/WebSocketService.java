package com.nova.poneglyph.service;

import com.nova.poneglyph.config.websocket.WebSocketController;
import com.nova.poneglyph.domain.enums.DeliveryStatus;
import com.nova.poneglyph.domain.message.Call;
import com.nova.poneglyph.dto.NotificationDto;

import com.nova.poneglyph.dto.conversation.MessageDTO;
import com.nova.poneglyph.dto.conversation.ConversationDTO;
import com.nova.poneglyph.dto.websocket.PresenceEvent;
import com.nova.poneglyph.dto.websocket.TypingIndicator;
import com.nova.poneglyph.events.PresenceMessage;
import com.nova.poneglyph.events.TypingMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

    // إرسال رسالة عادية للمحادثة
    public void notifyNewMessage(UUID conversationId, Object messageDto) {
        try {
            messagingTemplate.convertAndSend("/topic/conversation." + conversationId, messageDto);
        } catch (Exception e) {
            log.error("Failed to send message to conversation {}: {}", conversationId, e.getMessage());
        }
    }

    public void notifyMessageStatus(String user, UUID messageId, String status) {
        try {
            messagingTemplate.convertAndSendToUser(user, "/queue/message-status", Map.of("messageId", messageId, "status", status));
        } catch (Exception e) {
            log.warn("Failed to send message status to user {}: {}", user, e.getMessage());
        }
    }

//    public void notifyMessageDeliveryStatus(UUID messageId, UUID conversationId, UUID userId, DeliveryStatus status) {
//        CompletableFuture.runAsync(() -> {
//            try {
//                Map<String, Object> payload = Map.of(
//                        "messageId", messageId,
//                        "conversationId", conversationId,
//                        "userId", userId,
//                        "status", status.toString(),
//                        "timestamp", System.currentTimeMillis()
//                );
//
//                // إرسال إلى محادثة محددة
//                messagingTemplate.convertAndSend(
//                        "/topic/message-status." + conversationId,
//                        payload
//                );
//
//                // إرسال إلى مستخدم محدد
//                messagingTemplate.convertAndSendToUser(
//                        userId.toString(),
//                        "/queue/message-status",
//                        payload
//                );
//
//                log.debug("Message status update sent for message {}: {}", messageId, status);
//            } catch (Exception e) {
//                log.error("Failed to send message status update for message {}: {}", messageId, e.getMessage());
//            }
//        }, asyncExecutor);
//    }

    public void notifyMessageDeliveryStatus(UUID messageId, UUID conversationId, UUID userId, DeliveryStatus status) {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> payload = new HashMap<>();

                // إضافة messageId فقط إذا لم يكن null
                if (messageId != null) {
                    payload.put("messageId", messageId.toString());
                }

                payload.put("conversationId", conversationId.toString());
                payload.put("userId", userId.toString());
                payload.put("status", status.toString());
                payload.put("timestamp", System.currentTimeMillis());

                // إرسال إلى محادثة محددة
                messagingTemplate.convertAndSend(
                        "/topic/message-status." + conversationId,
                        payload
                );

                // إرسال إلى مستخدم محدد
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/message-status",
                        payload
                );

                log.debug("Message status update sent for conversation {}: {}", conversationId, status);

            } catch (Exception e) {
                log.error("Failed to send message status update for conversation {}: {}", conversationId, e.getMessage());
            }
        }, asyncExecutor);
    }
    public void notifyMessageReadStatus(UUID messageId, UUID conversationId, UUID userId, DeliveryStatus status) {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> payload = new HashMap<>();

                // إضافة messageId فقط إذا لم يكن null
                if (messageId != null) {
                    payload.put("messageId", messageId.toString());
                }

                payload.put("conversationId", conversationId.toString());
                payload.put("userId", userId.toString());
                payload.put("status", status.toString());
                payload.put("timestamp", System.currentTimeMillis());

                // إرسال إلى محادثة محددة
                messagingTemplate.convertAndSend(
                        "/topic/message-status." + conversationId,
                        payload
                );

                // إرسال إلى مستخدم محدد
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/message-status",
                        payload
                );

                log.debug("Message status update sent for conversation {}: {}", conversationId, status);

            } catch (Exception e) {
                log.error("Failed to send message status update for conversation {}: {}", conversationId, e.getMessage());
            }
        }, asyncExecutor);
    }
    // إشعار تعديل رسالة
    public void notifyMessageEdited(UUID conversationId, MessageDTO updatedMessageDto) {
        try {
            messagingTemplate.convertAndSend("/topic/conversation." + conversationId + ".message.edited", updatedMessageDto);
        } catch (Exception e) {
            log.warn("Failed to send message edited event for conv {}: {}", conversationId, e.getMessage());
        }
    }

    // إشعار حذف رسالة (forEveryone)
    public void notifyMessageDeleted(UUID messageId, boolean forEveryone) {
        try {
            messagingTemplate.convertAndSend("/topic/message-deleted", Map.of("messageId", messageId, "forEveryone", forEveryone));
        } catch (Exception e) {
            log.warn("Failed to send message deleted event: {}", e.getMessage());
        }
    }

    public void sendNotification(UUID userId, NotificationDto notification) {
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notification", notification);
    }

    public void notifyConversationCreated(ConversationDTO conversation) {
        if (conversation == null || conversation.getId() == null) return;
        messagingTemplate.convertAndSend("/topic/conversations.created", conversation);
    }

    public void notifyConversationUpdated(UUID conversationId, ConversationDTO conversation) {
        if (conversationId == null) return;
        messagingTemplate.convertAndSend("/topic/conversation." + conversationId + ".meta", conversation);
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

    public void notifyReactionUpdate(UUID messageId, UUID userId, String reaction) {
        messagingTemplate.convertAndSend(
                "/topic/message." + messageId + ".reactions",
                Map.of("messageId", messageId, "userId", userId, "reaction", reaction)
        );
    }

    public void notifyUserStatusChange(UUID userId, String status) {
        messagingTemplate.convertAndSend(
                "/topic/user-status." + userId,
                Map.of("userId", userId, "status", status)
        );
    }

    public void sendNotificationToAdmins(String message) {
        messagingTemplate.convertAndSend(
                "/topic/admin/notifications",
                Map.of("message", message, "timestamp", System.currentTimeMillis())
        );
    }

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

    public void notifyTyping(UUID conversationId, TypingIndicator typingMessage) {
        CompletableFuture.runAsync(() -> {
            try {
                messagingTemplate.convertAndSend(
                        "/topic/conversation." + typingMessage.getConversationId() + ".typing",
                        typingMessage
                );

                messagingTemplate.convertAndSend(
                        "/topic/typing-global",
                        typingMessage
                );

                log.debug("Typing indicator sent for conversation {}: {}", conversationId, typingMessage.isTyping());
            } catch (Exception e) {
                log.error("Failed to send typing indicator to conversation {}: {}", conversationId, e.getMessage());
            }
        }, asyncExecutor);
    }

    public void notifyPresenceChange(UUID userId, boolean online, String status) {
        CompletableFuture.runAsync(() -> {
            try {
                PresenceEvent presenceEvent = new PresenceEvent();
                presenceEvent.setUserId(userId);
                presenceEvent.setOnline(online);
                presenceEvent.setStatus(status);
                presenceEvent.setTimestamp(System.currentTimeMillis());

                messagingTemplate.convertAndSend(
                        "/topic/presence",
                        presenceEvent
                );

                messagingTemplate.convertAndSend(
                        "/topic/presence." + userId,
                        presenceEvent
                );

                log.debug("Presence change sent for user {}: online={}, status={}", userId, online, status);
            } catch (Exception e) {
                log.error("Failed to send presence change for user {}: {}", userId, e.getMessage());
            }
        }, asyncExecutor);
    }

    public void notifyTyping(UUID conversationId, UUID userId, boolean isTyping) {
        CompletableFuture.runAsync(() -> {
            try {
                TypingMessage typingMessage = new TypingMessage();
                typingMessage.setConversationId(conversationId);
                typingMessage.setUserId(userId);
                typingMessage.setTyping(isTyping);
                typingMessage.setTimestamp(System.currentTimeMillis());

                messagingTemplate.convertAndSend(
                        "/topic/conversation." + conversationId + ".typing",
                        typingMessage
                );
            } catch (Exception e) {
                log.error("Failed to send typing indicator for conversation {}: {}", conversationId, e.getMessage());
            }
        }, asyncExecutor);
    }

    public void notifyPresence(UUID userId, boolean online, String status) {
        CompletableFuture.runAsync(() -> {
            try {
                PresenceMessage presenceMessage = new PresenceMessage();
                presenceMessage.setUserId(userId);
                presenceMessage.setOnline(online);
                presenceMessage.setStatus(status);
                presenceMessage.setTimestamp(System.currentTimeMillis());

                messagingTemplate.convertAndSend(
                        "/topic/presence." + userId,
                        presenceMessage
                );
            } catch (Exception e) {
                log.error("Failed to send presence update for user {}: {}", userId, e.getMessage());
            }
        }, asyncExecutor);
    }
}
