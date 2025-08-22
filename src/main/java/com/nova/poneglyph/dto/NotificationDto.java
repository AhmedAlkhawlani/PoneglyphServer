package com.nova.poneglyph.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private String title;
    private String body;
    private Map<String, String> data;
}


