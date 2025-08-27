package com.nova.poneglyph.dto.conversation;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SendMessageRequest {
    private String messageType;
    private String content;
    private List<MediaRequest> mediaAttachments;
    private UUID replyToId;
}
