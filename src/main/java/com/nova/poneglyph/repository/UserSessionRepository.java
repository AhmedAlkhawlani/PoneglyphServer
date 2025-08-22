package com.nova.poneglyph.repository;


import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserDevice;
import com.nova.poneglyph.domain.user.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {



        // ... طرق أخرى
        // إرجاع كل الجلسات المرتبطة بالمستخدم (قائمة)
        List<UserSession> findByUser_Id(UUID userId);

    // جلب الجلسات النشطة فقط
    List<UserSession> findByUser_IdAndActiveTrue(UUID userId);

    Optional<UserSession> findByRefreshTokenHash(String hash);

//    Optional<UserSession> findByActiveJti(UUID jti);

    // JPQL مصحح: استخدم user.id لأن حقل الـ User اسمه id في الـ entity
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = true")
    List<UserSession> findActiveSessionsByUser(@Param("userId") UUID userId);

    // تعطيل جميع الجلسات (single update query) — أسرع من حلقة حفظ متعددة
    @Transactional
    @Modifying
    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId")
    void revokeAllForUser(@Param("userId") UUID userId);

    // حذف جلسات المستخدم بالكامل
    @Transactional
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Query("SELECT s FROM UserSession s WHERE s.websocketSessionId = :websocketSessionId")
    Optional<UserSession> findByWebsocketSessionId(@Param("websocketSessionId") String websocketSessionId);

    @Transactional
    @Modifying
    @Query("UPDATE UserSession s SET s.online = :online WHERE s.id = :sessionId")
    void updateOnlineStatus(@Param("sessionId") UUID sessionId, @Param("online") boolean online);

    @Query("SELECT s FROM UserSession s WHERE s.lastActivity < :threshold AND s.active = true")
    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);


//    Optional<UserSession> findByUserId(UUID userId);
    List<UserSession> findByUserIdAndActiveTrue(UUID userId);
    Optional<UserSession> findByActiveJti(UUID jti);

    Optional<UserSession> findByUser_NormalizedPhone(String normalizedPhone);
    @Modifying
    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId AND s.active = true")
    void revokeAllActiveSessionsForUser(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId AND s.id <> :exceptSessionId")
    void revokeAllSessionsExcept(@Param("userId") UUID userId, @Param("exceptSessionId") UUID exceptSessionId);

//    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = true ORDER BY s.issuedAt DESC")
//    List<UserSession> findActiveSessionsByUserId(@Param("userId") UUID userId);

    Optional<UserSession> findByUserAndDevice(User user, UserDevice device);

    // تغيير هذه الدالة لترجع قائمة بدلاً من Optional واحد
    List<UserSession> findByUserId(UUID userId);

    // إضافة دالة جديدة للبحث عن الجلسة النشطة
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = true ORDER BY s.lastUsedAt DESC")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") UUID userId);

    // دالة للبحث عن جلسة واحدة نشطة (الأحدث)
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = true ORDER BY s.lastUsedAt DESC LIMIT 1")
    Optional<UserSession> findLatestActiveSessionByUserId(@Param("userId") UUID userId);


//    @Modifying
//    @Query("delete from UserSession s where s.user.id = :userId")
//    void deleteByUserId(UUID userId);
}
