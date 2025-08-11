//package com.nova.poneglyph.message;
//
//import com.nova.poneglyph.chat.Chat;
//import com.nova.poneglyph.common.BaseAuditingEntity;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.GenericGenerator;
//
//import java.io.File;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Entity
//@Table(name = "messages")
//@NamedQuery(name = MessageConstants.FIND_MESSAGES_BY_CHAT_ID,
//            query = "SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdDate"
//)
//@NamedQuery(name = MessageConstants.SET_MESSAGES_TO_SEEN_BY_CHAT,
//            query = "UPDATE Message SET state = :newState WHERE chat.id = :chatId"
//)
////@Builder
//public class Message extends BaseAuditingEntity {
//
////    @Id
////    @SequenceGenerator(name = "msg_seq", sequenceName = "msg_seq", allocationSize = 1)
////    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "msg_seq")
//    @Id
//    @GeneratedValue(generator = "UUID")
//    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
//    private String id;
//    @Column(columnDefinition = "TEXT")
//    private String content;
//    @Enumerated(EnumType.STRING)
//    private MessageState state;
//    @Enumerated(EnumType.STRING)
//    private MessageType type;
//    @ManyToOne
//    @JoinColumn(name = "chat_id")
//    private Chat chat;
//    @Column(name = "sender_id", nullable = false)
//    private String senderId;
//    @Column(name = "receiver_id", nullable = false)
//    private String receiverId;
//    private String mediaFilePath;
//
//
//}
