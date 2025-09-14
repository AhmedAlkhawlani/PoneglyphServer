package com.nova.poneglyph.dto.contactDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContactDto {
    private String phone;
    private String name;
    private boolean registered; // بدون "is"
    private boolean online;
    private OffsetDateTime lastSeen;
    private boolean blocked; // بدون "is"
    private String registeredId;

}
