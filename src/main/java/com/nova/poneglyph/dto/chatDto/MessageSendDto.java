package com.nova.poneglyph.dto.chatDto;

import com.nova.poneglyph.domain.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class MessageSendDto {
    @NotNull
    private UUID conversationId;

    @NotNull
    private String content;

    @NotNull
    private MessageType messageType;

    private boolean encrypt;
    private UUID replyToId;
    private Set<MediaAttachmentDto> mediaAttachments;
}
