//package com.nova.poneglyph.repository;
//
//import com.nova.poneglyph.model.Conversation;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface ConversationRepository extends JpaRepository<Conversation, String> {
//    // إضافة هذا الاستعلام للبحث عن المحادثات التي تحتوي على رقم معين
//    @Query("SELECT c FROM conversation c WHERE :phoneNumber MEMBER OF c.participantIds")
//    List<Conversation> findByParticipantPhonesContaining(@Param("phoneNumber") String phoneNumber);
//
//    // النسخة الأصلية مع Pagination
//    // البحث عن محادثات مستخدم معين
//    @Query("SELECT c FROM conversation c WHERE :phoneNumber MEMBER OF c.participantIds")
//    Page<Conversation> findByParticipantPhonesContaining(String phoneNumber, Pageable pageable);
//
//    // البحث عن محادثة بين مستخدمين محددين (شخص لشخص)
//    @Query("SELECT c FROM conversation c " +
//            "WHERE c.isGroup = false " +
//            "AND :phone1 MEMBER OF c.participantIds " +
//            "AND :phone2 MEMBER OF c.participantIds")
//    List<Conversation> findConversationBetweenUsers(String phone1, String phone2);
//
//    // تحديث وقت التعديل الأخير للمحادثة
//    @Query("UPDATE conversation c SET c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :conversationId")
//    void updateConversationTimestamp(String conversationId);
//}
