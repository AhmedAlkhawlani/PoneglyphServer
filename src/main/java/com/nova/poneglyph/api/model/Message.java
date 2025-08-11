package com.nova.poneglyph.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String from;
    private String to;
    private String content;
    private MessageType type;
    private String timestamp;
    private String room;
    private String replyTo;
    private String fileBase64;
    private String fileType;
}
