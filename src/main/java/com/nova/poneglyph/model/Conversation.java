package com.nova.poneglyph.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

//package com.nova.poneglyph.entity;
//
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.GenericGenerator;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//
//@Entity(name = "conversation")
//@Getter
//@Setter
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//public class Conversation {
//    @Id
//    @GeneratedValue(generator = "UUID")
//    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
//    private String id;
//
//    private boolean isGroup;
//    private String groupName;
//    private String createdBy;
//    @Column(name = "created_at") // <== هذا السطر هو المفتاح
//    private String createdAt;
//    private LocalDateTime updatedAt;
//
//    @ElementCollection
//    @CollectionTable(name = "conversation_participants", joinColumns = @JoinColumn(name = "conversation_id"))
//    @Column(name = "participant_id")
//    private List<String> participantIds;
//
//    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
//    private List<Message> messages;
//
//}
@Entity(name = "conversation")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(nullable = false)
    private boolean isGroup = false; // دائماً false للمحادثات الفردية

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "conversation_participants",
            joinColumns = @JoinColumn(name = "conversation_id"))
    @Column(name = "user_id")
    private List<String> participantIds; // رقمين الهاتف فقط

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sentAt ASC") // ترتيب الرسائل حسب التاريخ
    private List<Message> messages;

    @Transient
    public String getOtherParticipant(String currentUserId) {
        return participantIds.stream()
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElse(null);
    }
}
