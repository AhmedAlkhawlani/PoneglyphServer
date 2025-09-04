////package com.nova.poneglyph.repository;
////
////import com.nova.poneglyph.domain.user.User;
////import com.nova.poneglyph.domain.user.UserDevice;
////import com.nova.poneglyph.domain.user.UserSession;
////import org.springframework.data.jpa.repository.JpaRepository;
////import org.springframework.data.jpa.repository.Modifying;
////import org.springframework.data.jpa.repository.Query;
////import org.springframework.data.repository.query.Param;
////import org.springframework.stereotype.Repository;
////
////import java.time.OffsetDateTime;
////import java.util.List;
////import java.util.Optional;
////import java.util.UUID;
////
////@Repository
////public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
////
//////    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = true ORDER BY s.lastUsedAt DESC LIMIT 1")
//////    Optional<UserSession> findLatestActiveSessionByUserId(@Param("userId") UUID userId);
////
////
////    Optional<UserSession> findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(String normalizedPhone);
////
////    // إضافة الدوال المطلوبة
////    List<UserSession> findByIssuedAtAfter(OffsetDateTime issuedAt);
////
////    List<UserSession> findByIssuedAtBetween(OffsetDateTime start, OffsetDateTime end);
////
////    List<UserSession> findByUser_IdAndActiveTrue(UUID userId);
////
////    Optional<UserSession> findByUserAndDevice(User user, UserDevice device);
////
////    Optional<UserSession> findTop1ByUser_IdAndActiveTrueOrderByLastUsedAtDesc(UUID userId);
////
////    Optional<UserSession> findLatestActiveSessionByUserId(UUID userId);
////
////    List<UserSession> findActiveSessionsByUserId(UUID userId);
////
////    Optional<UserSession> findByActiveJti(UUID jti);
////
////    Optional<UserSession> findByWebsocketSessionId(String websocketSessionId);
////
////    /**
////     *
////     الجلسات النشطة التي لم يتم استخدامها منذ وقت معين
////     أو الجلسات غير المتصلة التي لم يكن لديها نشاط منذ وقت معين
////     * */
//////    @Query("SELECT s FROM UserSession s WHERE " +
//////            "(s.active = true AND s.lastUsedAt < :threshold) OR " +
//////            "(s.online = false AND s.lastActivity < :threshold)")
//////    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);
////    /**
////     *
////     * لم يكن لديها نشاط منذ وقت معين (threshold)
////     * أو الجلسات النشطة التي لم يتم استخدامها منذ وقت معين
////     * */
//////    @Query("SELECT s FROM UserSession s WHERE s.lastActivity < :threshold OR (s.active = true AND s.lastUsedAt < :threshold)")
//////    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);
////    @Query("SELECT s FROM UserSession s WHERE " +
////            "s.online = true AND s.lastActivity < :threshold")
////    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);
////
////
////    @Modifying
////    @Query("UPDATE UserSession s SET s.online = :online WHERE s.id = :sessionId")
////    void updateOnlineStatus(@Param("sessionId") UUID sessionId, @Param("online") boolean online);
////
////    @Modifying
////    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId")
////    void revokeAllForUser(@Param("userId") UUID userId);
////
////    @Modifying
////    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId AND s.active = true")
////    void revokeAllActiveSessionsForUser(@Param("userId") UUID userId);
////
////    Optional<UserSession> findByUser_Id(UUID userId);
////
////    @Query("SELECT us FROM UserSession us WHERE us.active = true")
////    List<UserSession> findAllActiveSessions();
////}
//
//package com.nova.poneglyph.repository;
//
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.domain.user.UserDevice;
//import com.nova.poneglyph.domain.user.UserSession;
//import jakarta.persistence.LockModeType;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Lock;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository
//public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
//    // إيجاد أحدث جلسة نشطة للمستخدم
//    Optional<UserSession> findTop1ByUser_IdAndActiveTrueOrderByLastUsedAtDesc(UUID userId);
//
//    // إيجاد جلسة حسب websocketSessionId
//    Optional<UserSession> findByWebsocketSessionId(String websocketSessionId);
//
//    // قراءة صف مع قفل PESSIMISTIC_WRITE لاستخدامه عند عمليات الـ disconnect/cleanup
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("select s from UserSession s where s.id = :id")
//    Optional<UserSession> findByIdForUpdate(@Param("id") UUID id);
//
//    // إيجاد الجلسات غير النشطة قبل threshold
//    @Query("select s from UserSession s where s.active = true and s.lastActivity < :threshold")
//    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);
//
//    // revoke جميع جلسات المستخدم (تحديث كتل)
//    @Modifying
//    @Transactional
//    @Query("update UserSession s set s.active = false where s.user.id = :userId")
//    void revokeAllForUser(@Param("userId") UUID userId);
//
//    @Modifying
//    @Transactional
//    @Query("update UserSession s set s.active = false where s.user.id = :userId and s.active = true")
//    void revokeAllActiveSessionsForUser(@Param("userId") UUID userId);
//
//    // إرجاع كل الجلسات النشطة (لمهام الصيانة)
//    @Query("select s from UserSession s where s.active = true")
//    List<UserSession> findAllActiveSessions();
//
//    // تحديث حالة online في DB
//    @Modifying
//    @Transactional
//    @Query("update UserSession s set s.online = :online where s.id = :sessionId")
//    int updateOnlineStatus(@Param("sessionId") UUID sessionId, @Param("online") boolean online);
//
//    // إيجاد جلسة بواسطة activeJti
//    Optional<UserSession> findByActiveJti(UUID jti);
//
//    // إيجاد جلسة بواسطة user+device
//    Optional<UserSession> findByUserAndDevice(User user, UserDevice device);
//    Optional<UserSession> findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(String normalizedPhone);
//
//    List<UserSession> findByIssuedAtAfter(OffsetDateTime issuedAt);
//
//    List<UserSession> findByIssuedAtBetween(OffsetDateTime start, OffsetDateTime end);
//
//    List<UserSession> findByUser_IdAndActiveTrue(UUID userId);
//
//
////    Optional<UserSession> findTop1ByUser_IdAndActiveTrueOrderByLastUsedAtDesc(UUID userId);
//
//    Optional<UserSession> findLatestActiveSessionByUserId(UUID userId);
//
//    List<UserSession> findActiveSessionsByUserId(UUID userId);
//
////    Optional<UserSession> findByActiveJti(UUID jti);
//
////    Optional<UserSession> findByWebsocketSessionId(String websocketSessionId);
//
//    // استعلام محسن للجلسات غير النشطة
////    @Query("SELECT s FROM UserSession s WHERE " +
////            "s.online = true AND s.lastActivity < :threshold")
////    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);
//
////    @Modifying
////    @Query("UPDATE UserSession s SET s.online = :online WHERE s.id = :sessionId")
////    void updateOnlineStatus(@Param("sessionId") UUID sessionId, @Param("online") boolean online);
//
////    @Modifying
////    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId")
////    void revokeAllForUser(@Param("userId") UUID userId);
////
////    @Modifying
////    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId AND s.active = true")
////    void revokeAllActiveSessionsForUser(@Param("userId") UUID userId);
//
//    Optional<UserSession> findByUser_Id(UUID userId);
//
////    @Query("SELECT us FROM UserSession us WHERE us.active = true")
////    List<UserSession> findAllActiveSessions();
//}
package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserDevice;
import com.nova.poneglyph.domain.user.UserSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findTop1ByUser_IdAndActiveTrueOrderByLastUsedAtDesc(UUID userId);

    Optional<UserSession> findByWebsocketSessionId(String websocketSessionId);

    // method signature must use correct type for device
    Optional<UserSession> findByUserAndDevice(User user, UserDevice device);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from UserSession s where s.id = :id")
    Optional<UserSession> findByIdForUpdate(@Param("id") UUID id);

    @Query("select s from UserSession s where s.active = true and s.lastActivity < :threshold")
    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);

    @Modifying
    @Transactional
    @Query("update UserSession s set s.websocketSessionId = null, s.online = false, s.lastActivity = :lastActivity where s.id = :id")
    int updateClearWebsocketSessionId(@Param("id") UUID id, @Param("lastActivity") OffsetDateTime lastActivity);

    @Modifying
    @Transactional
    @Query("update UserSession s set s.lastActivity = :lastActivity where s.id = :id")
    int updateLastActivity(@Param("id") UUID id, @Param("lastActivity") OffsetDateTime lastActivity);

    @Modifying
    @Transactional
    @Query("update UserSession s set s.active = false where s.user.id = :userId")
    void revokeAllForUser(@Param("userId") UUID userId);

    @Query("select s from UserSession s where s.active = true")
    List<UserSession> findAllActiveSessions();

    // Example supporting method used in SessionService revoke by jti
    Optional<UserSession> findByActiveJti(UUID jti);
        Optional<UserSession> findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(String normalizedPhone);
    List<UserSession> findByUser_IdAndActiveTrue(UUID userId);

        List<UserSession> findActiveSessionsByUserId(UUID userId);
    List<UserSession> findByIssuedAtBetween(OffsetDateTime start, OffsetDateTime end);

        @Modifying
    @Transactional
    @Query("update UserSession s set s.active = false where s.user.id = :userId and s.active = true")
    void revokeAllActiveSessionsForUser(@Param("userId") UUID userId);
//

}
