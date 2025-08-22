package com.nova.poneglyph.dto.callDto;



import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CallInitiateDto {
    private UUID receiverId;
    private UUID conversationId;

    @NotNull
    private String callType;

    private boolean recorded;
}

