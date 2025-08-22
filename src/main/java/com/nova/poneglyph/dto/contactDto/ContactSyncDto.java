package com.nova.poneglyph.dto.contactDto;



import lombok.Data;

import java.util.List;

@Data
public class ContactSyncDto {
    private List<ContactDto> contacts;
}

