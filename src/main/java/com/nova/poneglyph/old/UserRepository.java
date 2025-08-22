//package com.nova.poneglyph.repository;
//
//import com.nova.poneglyph.enums.old.user.enums.Active;
//import com.nova.poneglyph.model.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface UserRepository extends JpaRepository<User, String> {
//
////    Optional<User> findByEmail(String email);
//
////    Optional<User> findByUsername(String username);
//
//    List<User> findAllByActive(Active active);
//
//
//    // البحث برقم الهاتف (مع فهرس)
//    @Query("SELECT u FROM users u WHERE u.phoneNumber = :phoneNumber")
//    Optional<User> findByPhoneNumber(String phoneNumber);
//
//    // البحث بالبريد الإلكتروني (مع فهرس)
//    Optional<User> findByEmail(String email);
//
//    // التحقق من وجود رقم هاتف
//    boolean existsByPhoneNumber(String phoneNumber);
//
//    // تحديث حالة الاتصال
//    @Modifying
//    @Query("UPDATE users u SET u.online = :online, u.lastSeen = CASE WHEN :online THEN NULL ELSE CURRENT_TIMESTAMP END WHERE u.id = :userId")
//    void updateUserPresence(String userId, boolean online);
//
//    // تحديث معرف جلسة الويب سوكيت
//    @Modifying
//    @Query("UPDATE users u SET u.currentWebSocketSessionId = :sessionId WHERE u.id = :userId")
//    void updateWebSocketSessionId(String userId, String sessionId);
//}
