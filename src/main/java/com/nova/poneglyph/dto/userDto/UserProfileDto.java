package com.nova.poneglyph.dto.userDto;



import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UserProfileDto {
    private UUID userId;
    private String displayName;
    private String avatarUrl;
    private String aboutText;
    private String statusEmoji;
    private OffsetDateTime lastProfileUpdate;
}
