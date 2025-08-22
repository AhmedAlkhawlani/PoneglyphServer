//package com.nova.poneglyph.dto;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
////import com.nova.poneglyph.enums.old.MessageStatus;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class MessageDTO {
//    private String id;
//    private String conversationId;
//    private String senderPhone;
//    private String content;
//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
//    private LocalDateTime sentAt;
//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
//    private LocalDateTime deliveredAt;
//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
//    private LocalDateTime seenAt;
////    private MessageStatus status;
//    /// إضافة حقل الرد
//    private String replyToId; // إضافة حقل الرد
//    private MessageDTO repliedMessage; // يمكن إضافته عند جلب الرسائل
//
//
//}
