//package com.nova.poneglyph.model;
//
//
//import com.nova.poneglyph.enums.old.MessageStatus;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.GenericGenerator;
//
//
//import java.time.LocalDateTime;
//
////@Builder
////@AllArgsConstructor
////@NoArgsConstructor
////@Entity(name = "message")
////@Getter
////@Setter
////public class Message {
////
////    @Id
////    @GeneratedValue(generator = "UUID")
////    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
////    private String id;
////
////    @ManyToOne
////    private Conversation conversation;
////
////    private String senderId;
////    private String content;
//////    private String sender;
////    private String receiverId;
////    private boolean seen;
////    private boolean delivered;
////    //System.currentTimeMillis()
////    private Long timestamp;
////    private LocalDateTime sentAt;
////    private LocalDateTime seenAt;
////
////    @Enumerated(EnumType.STRING)
////    private MessageStatus status;
////}
//@Entity(name = "message")
//@Getter
//@Setter
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//public class Message {
//    @Id
//    @GeneratedValue(generator = "UUID")
//    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
//    private String id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "conversation_id", nullable = false)
//    private Conversation conversation;
//
//    @Column(nullable = false)
//    private String senderPhone; // رقم هاتف المرسل
//
//    @Column(nullable = false, columnDefinition = "TEXT")
//    private String content;
//
//    @CreationTimestamp
//    private LocalDateTime sentAt;
//
//    private LocalDateTime deliveredAt;
//
//    private LocalDateTime seenAt;
//
//    @Enumerated(EnumType.STRING)
//    private MessageStatus status = MessageStatus.SENT;
//
//    // فهرس لتحسين أداء الاستعلامات
////    @Index(name = "idx_message_sender")
//    private String senderPhoneIndex;
//
////    @Index(name = "idx_message_conversation")
//    private String conversationIdIndex;
//
//    @PrePersist
//    @PreUpdate
//    private void setIndexes() {
//        this.senderPhoneIndex = this.senderPhone;
//        this.conversationIdIndex = this.conversation != null ? this.conversation.getId() : null;
//    }
//}
