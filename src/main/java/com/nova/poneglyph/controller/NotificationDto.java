package com.nova.poneglyph.controller;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class NotificationDto {
//    private String senderUsername;
//    private String receiverUsername;
//    private String message;
//    private String type; // مثل "MESSAGE", "TYPING", "SEEN", "DELIVERED"
//    private String timestamp;
//}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private String senderId;
    private String receiverId;
    private String message;
    private String type; // مثل: MESSAGE, SEEN, TYPING, etc.
    private Long timestamp;
}

