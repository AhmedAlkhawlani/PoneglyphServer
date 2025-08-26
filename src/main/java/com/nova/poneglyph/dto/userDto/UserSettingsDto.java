package com.nova.poneglyph.dto.userDto;





import lombok.Data;

@Data
public class UserSettingsDto {
    private boolean messageNotifications;
    private boolean callNotifications;
    private boolean groupNotifications;
    private boolean onlineStatusVisible;
    private boolean readReceipts;
    private String theme;
    private String language;
}

