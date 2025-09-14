package com.nova.poneglyph.dto.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

@Data
public class TypingIndicator {
    private UUID conversationId;
    private UUID userId;
    // يتم تعبئته على السيرفر من Authentication
    @JsonProperty("isTyping")
    private boolean isTyping; // Jackson سيطابق "isTyping" من JSON بشكل صحيح إلى هذا الحقل
    private Long timestamp;
}
