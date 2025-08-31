//package com.nova.poneglyph.repository;
//
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.domain.user.UserDevice;
//import com.nova.poneglyph.domain.user.UserSession;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository
//public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
//
////    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = true ORDER BY s.lastUsedAt DESC LIMIT 1")
////    Optional<UserSession> findLatestActiveSessionByUserId(@Param("userId") UUID userId);
//
//
//    Optional<UserSession> findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(String normalizedPhone);
//
//    // إضافة الدوال المطلوبة
//    List<UserSession> findByIssuedAtAfter(OffsetDateTime issuedAt);
//
//    List<UserSession> findByIssuedAtBetween(OffsetDateTime start, OffsetDateTime end);
//
//    List<UserSession> findByUser_IdAndActiveTrue(UUID userId);
//
//    Optional<UserSession> findByUserAndDevice(User user, UserDevice device);
//
//    Optional<UserSession> findTop1ByUser_IdAndActiveTrueOrderByLastUsedAtDesc(UUID userId);
//
//    Optional<UserSession> findLatestActiveSessionByUserId(UUID userId);
//
//    List<UserSession> findActiveSessionsByUserId(UUID userId);
//
//    Optional<UserSession> findByActiveJti(UUID jti);
//
//    Optional<UserSession> findByWebsocketSessionId(String websocketSessionId);
//
//    /**
//     *
//     الجلسات النشطة التي لم يتم استخدامها منذ وقت معين
//     أو الجلسات غير المتصلة التي لم يكن لديها نشاط منذ وقت معين
//     * */
////    @Query("SELECT s FROM UserSession s WHERE " +
////            "(s.active = true AND s.lastUsedAt < :threshold) OR " +
////            "(s.online = false AND s.lastActivity < :threshold)")
////    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);
//    /**
//     *
//     * لم يكن لديها نشاط منذ وقت معين (threshold)
//     * أو الجلسات النشطة التي لم يتم استخدامها منذ وقت معين
//     * */
////    @Query("SELECT s FROM UserSession s WHERE s.lastActivity < :threshold OR (s.active = true AND s.lastUsedAt < :threshold)")
////    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);
//    @Query("SELECT s FROM UserSession s WHERE " +
//            "s.online = true AND s.lastActivity < :threshold")
//    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);
//
//
//    @Modifying
//    @Query("UPDATE UserSession s SET s.online = :online WHERE s.id = :sessionId")
//    void updateOnlineStatus(@Param("sessionId") UUID sessionId, @Param("online") boolean online);
//
//    @Modifying
//    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId")
//    void revokeAllForUser(@Param("userId") UUID userId);
//
//    @Modifying
//    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId AND s.active = true")
//    void revokeAllActiveSessionsForUser(@Param("userId") UUID userId);
//
//    Optional<UserSession> findByUser_Id(UUID userId);
//
//    @Query("SELECT us FROM UserSession us WHERE us.active = true")
//    List<UserSession> findAllActiveSessions();
//}

package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserDevice;
import com.nova.poneglyph.domain.user.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(String normalizedPhone);

    List<UserSession> findByIssuedAtAfter(OffsetDateTime issuedAt);

    List<UserSession> findByIssuedAtBetween(OffsetDateTime start, OffsetDateTime end);

    List<UserSession> findByUser_IdAndActiveTrue(UUID userId);

    Optional<UserSession> findByUserAndDevice(User user, UserDevice device);

    Optional<UserSession> findTop1ByUser_IdAndActiveTrueOrderByLastUsedAtDesc(UUID userId);

    Optional<UserSession> findLatestActiveSessionByUserId(UUID userId);

    List<UserSession> findActiveSessionsByUserId(UUID userId);

    Optional<UserSession> findByActiveJti(UUID jti);

    Optional<UserSession> findByWebsocketSessionId(String websocketSessionId);

    // استعلام محسن للجلسات غير النشطة
    @Query("SELECT s FROM UserSession s WHERE " +
            "s.online = true AND s.lastActivity < :threshold")
    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);

    @Modifying
    @Query("UPDATE UserSession s SET s.online = :online WHERE s.id = :sessionId")
    void updateOnlineStatus(@Param("sessionId") UUID sessionId, @Param("online") boolean online);

    @Modifying
    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId")
    void revokeAllForUser(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId AND s.active = true")
    void revokeAllActiveSessionsForUser(@Param("userId") UUID userId);

    Optional<UserSession> findByUser_Id(UUID userId);

    @Query("SELECT us FROM UserSession us WHERE us.active = true")
    List<UserSession> findAllActiveSessions();
}
