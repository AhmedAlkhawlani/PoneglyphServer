package com.nova.poneglyph.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    private String type; // NEW_MESSAGE, MESSAGE_DELIVERED, MESSAGE_SEEN, etc.
    private String senderPhone;
    private String receiverPhone;
    private String content;
    private Long timestamp;
    private Map<String, Object> additionalData;
}
