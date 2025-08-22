package com.nova.poneglyph.dto.chatDto;


import com.nova.poneglyph.domain.enums.ConversationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class ConversationCreateDto {
    @NotNull
    private ConversationType type;

    private String title;
    private Set<UUID> participantIds;
    private boolean isGroup;
}

