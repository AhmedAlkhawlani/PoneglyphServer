//package com.nova.poneglyph.dto;
//
//
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import lombok.*;
//
//import java.io.Serializable;
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class Notification implements Serializable {
//    private String userId;
//    private String message;
//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
//    private LocalDateTime timestamp;
//
//    private NotificationType type;
//
//    public enum NotificationType {
//        MESSAGE, SYSTEM, ALERT,DELIVERY, SEEN
//    }
//
//    private String notificationId;
//
//    private String relatedMessageId;
//
//
//}
