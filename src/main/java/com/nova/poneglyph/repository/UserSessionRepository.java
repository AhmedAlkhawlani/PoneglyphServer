//package com.nova.poneglyph.repository;
//
//
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.domain.user.UserDevice;
//import com.nova.poneglyph.domain.user.UserSession;
//import org.springframework.data.jpa.repository.JpaRepository;
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
//
//
//@Repository
//public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
//
//    List<UserSession> findByUser_Id(UUID userId);
//    List<UserSession> findByUser_IdAndActiveTrue(UUID userId);
//    Optional<UserSession> findByRefreshTokenHash(String hash);
//
//    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = true")
//    List<UserSession> findActiveSessionsByUser(@Param("userId") UUID userId);
//
//    @Transactional
//    @Modifying
//    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId")
//    void revokeAllForUser(@Param("userId") UUID userId);
//
//    @Transactional
//    @Modifying
//    @Query("DELETE FROM UserSession s WHERE s.user.id = :userId")
//    void deleteByUserId(@Param("userId") UUID userId);
//
//    Optional<UserSession> findByWebsocketSessionId(String websocketSessionId);
//
//    @Transactional
//    @Modifying
//    @Query("UPDATE UserSession s SET s.online = :online WHERE s.id = :sessionId")
//    void updateOnlineStatus(@Param("sessionId") UUID sessionId, @Param("online") boolean online);
//
//    @Query("SELECT s FROM UserSession s WHERE s.lastActivity < :threshold AND s.active = true")
//    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);
//
//    List<UserSession> findByUserIdAndActiveTrue(UUID userId);
//    Optional<UserSession> findByActiveJti(UUID jti);
//    Optional<UserSession> findByUser_NormalizedPhone(String normalizedPhone);
//
//    @Modifying
//    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId AND s.active = true")
//    void revokeAllActiveSessionsForUser(@Param("userId") UUID userId);
//
//    @Modifying
//    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId AND s.id <> :exceptSessionId")
//    void revokeAllSessionsExcept(@Param("userId") UUID userId, @Param("exceptSessionId") UUID exceptSessionId);
//
//    Optional<UserSession> findByUserAndDevice(User user, UserDevice device);
//    List<UserSession> findByUserId(UUID userId);
//
//    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = true ORDER BY s.lastUsedAt DESC")
//    List<UserSession> findActiveSessionsByUserId(@Param("userId") UUID userId);
//
//    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = true ORDER BY s.lastUsedAt DESC LIMIT 1")
//    Optional<UserSession> findLatestActiveSessionByUserId(@Param("userId") UUID userId);
//
//    // إضافة دالة للبحث عن الجلسات في نطاق زمني
//    List<UserSession> findByIssuedAtBetween(OffsetDateTime start, OffsetDateTime end);
//}
package com.nova.poneglyph.repository;

import aj.org.objectweb.asm.commons.Remapper;
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

//    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = true ORDER BY s.lastUsedAt DESC LIMIT 1")
//    Optional<UserSession> findLatestActiveSessionByUserId(@Param("userId") UUID userId);


    Optional<UserSession> findTop1ByUser_NormalizedPhoneOrderByLastUsedAtDesc(String normalizedPhone);

    // إضافة الدوال المطلوبة
    List<UserSession> findByIssuedAtAfter(OffsetDateTime issuedAt);

    List<UserSession> findByIssuedAtBetween(OffsetDateTime start, OffsetDateTime end);

    List<UserSession> findByUser_IdAndActiveTrue(UUID userId);

    Optional<UserSession> findByUserAndDevice(User user, UserDevice device);

    Optional<UserSession> findTop1ByUser_IdAndActiveTrueOrderByLastUsedAtDesc(UUID userId);

    Optional<UserSession> findLatestActiveSessionByUserId(UUID userId);

    List<UserSession> findActiveSessionsByUserId(UUID userId);

    Optional<UserSession> findByActiveJti(UUID jti);

    Optional<UserSession> findByWebsocketSessionId(String websocketSessionId);

    /**
     *
     الجلسات النشطة التي لم يتم استخدامها منذ وقت معين
     أو الجلسات غير المتصلة التي لم يكن لديها نشاط منذ وقت معين
     * */
//    @Query("SELECT s FROM UserSession s WHERE " +
//            "(s.active = true AND s.lastUsedAt < :threshold) OR " +
//            "(s.online = false AND s.lastActivity < :threshold)")
//    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);
    /**
     *
     * لم يكن لديها نشاط منذ وقت معين (threshold)
     * أو الجلسات النشطة التي لم يتم استخدامها منذ وقت معين
     * */
    @Query("SELECT s FROM UserSession s WHERE s.lastActivity < :threshold OR (s.active = true AND s.lastUsedAt < :threshold)")
    List<UserSession> findInactiveSessions(@Param("threshold") OffsetDateTime threshold);


//    List<UserSession> findInactiveSessions(OffsetDateTime threshold);

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
