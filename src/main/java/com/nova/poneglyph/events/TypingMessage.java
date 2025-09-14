package com.nova.poneglyph.events;

import lombok.Data;



import lombok.Data;
import java.util.UUID;

@Data
public class TypingMessage {
    private UUID conversationId;
    private boolean typing;
    private UUID userId;
    private long timestamp;

    // getters/setters
}
