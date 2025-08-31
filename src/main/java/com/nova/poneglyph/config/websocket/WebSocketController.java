package com.nova.poneglyph.config.websocket;

import com.nova.poneglyph.dto.chatDto.MessageDto;
import com.nova.poneglyph.service.WebSocketService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class WebSocketController {

    private final WebSocketService webSocketService;

    public WebSocketController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public MessageDto sendMessage(@Payload MessageDto chatMessage, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            UUID userId = UUID.fromString(authentication.getName());
            chatMessage.setSenderId(userId);

            // إرسال الرسالة إلى المحادثة المحددة
            webSocketService.notifyNewMessage(chatMessage.getConversationId(), chatMessage);

            return chatMessage;
        }
        throw new SecurityException("User not authenticated");
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public MessageDto addUser(@Payload MessageDto chatMessage, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            UUID userId = UUID.fromString(authentication.getName());
            chatMessage.setSenderId(userId);
            chatMessage.setContent(" joined the chat");
            return chatMessage;
        }
        throw new SecurityException("User not authenticated");
    }

    @MessageMapping("/call.initiate")
    public void initiateCall(@Payload CallRequest callRequest, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            UUID callerId = UUID.fromString(authentication.getName());

            // معالجة طلب المكالمة
            webSocketService.notifyIncomingCall(callRequest);
        }
    }

    @MessageMapping("/call.answer")
    public void answerCall(@Payload CallAnswer callAnswer, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            UUID answererId = UUID.fromString(authentication.getName());

            // معالجة إجابة المكالمة
            webSocketService.notifyCallStatus(callAnswer.getCallId(), callAnswer.getStatus());
        }
    }

    // DTO classes for WebSocket messages
    public static class CallRequest {
        private UUID callId;
        private UUID callerId;
        private UUID receiverId;
        private String type; // voice or video

        // getters and setters
        public UUID getCallId() { return callId; }
        public void setCallId(UUID callId) { this.callId = callId; }
        public UUID getCallerId() { return callerId; }
        public void setCallerId(UUID callerId) { this.callerId = callerId; }
        public UUID getReceiverId() { return receiverId; }
        public void setReceiverId(UUID receiverId) { this.receiverId = receiverId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class CallAnswer {
        private UUID callId;
        private String status; // accepted, rejected, missed

        // getters and setters
        public UUID getCallId() { return callId; }
        public void setCallId(UUID callId) { this.callId = callId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
