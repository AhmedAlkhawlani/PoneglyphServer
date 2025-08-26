package com.nova.poneglyph.service;

import com.nova.poneglyph.domain.message.Call;
import com.nova.poneglyph.dto.NotificationDto;
import com.nova.poneglyph.dto.chatDto.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

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

    public void notifyIncomingCall(com.nova.poneglyph.controller.websocket.WebSocketController.CallRequest callRequest) {
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
}
