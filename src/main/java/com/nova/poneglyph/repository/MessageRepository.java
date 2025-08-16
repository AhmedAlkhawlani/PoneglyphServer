package com.nova.poneglyph.repository;

import com.nova.poneglyph.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    // الحصول على رسائل محادثة معينة
    @Query("SELECT m FROM message m WHERE m.conversation.id = :conversationId ORDER BY m.sentAt DESC")
    Page<Message> findByConversationId(String conversationId, Pageable pageable);

    // تحديث حالة الرسائل إلى "تم التسليم"
    @Modifying
    @Query("UPDATE message m SET m.deliveredAt = :deliveredAt, m.status = 'DELIVERED' " +
            "WHERE m.conversation.id = :conversationId AND m.senderPhone != :receiverPhone AND m.deliveredAt IS NULL")
    void markMessagesAsDelivered(String conversationId, String receiverPhone, LocalDateTime deliveredAt);

    // تحديث حالة الرسائل إلى "تمت المشاهدة"
    @Modifying
    @Query("UPDATE message m SET m.seenAt = :seenAt, m.status = 'SEEN' " +
            "WHERE m.conversation.id = :conversationId AND m.senderPhone != :viewerPhone AND m.seenAt IS NULL")
    void markMessagesAsSeen(String conversationId, String viewerPhone, LocalDateTime seenAt);

    // عد الرسائل غير المقروءة في محادثة
    @Query("SELECT COUNT(m) FROM message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.senderPhone != :userPhone " +
            "AND m.seenAt IS NULL")
    long countUnreadMessages(String conversationId, String userPhone);

//    String findSenderIdById(String id);
}
