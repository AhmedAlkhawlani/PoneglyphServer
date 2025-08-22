package com.nova.poneglyph.dto.contactDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@AllArgsConstructor
@Data
public class ContactDto {
    private String phone;
    private String name;
    private boolean isRegistered;
    private boolean online; // جديد

    private OffsetDateTime lastSeen;
    private boolean isBlocked;
}
