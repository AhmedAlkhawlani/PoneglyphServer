package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
public interface UserRepository extends JpaRepository<User, UUID> {


    Optional<User> findByPhoneNumber(String phone);


    @Query("SELECT u FROM User u WHERE u.lastActive < :threshold " +
            "AND u.accountStatus = 'ACTIVE'")
    List<User> findInactiveUsers(@Param("threshold") OffsetDateTime threshold);

    @Query("SELECT CASE WHEN COUNT(ub) > 0 THEN true ELSE false END " +
            "FROM UserBlock ub WHERE ub.blocker.id = :blockerId AND ub.blocked.id = :blockedId")
    boolean existsBlock(@Param("blockerId") UUID blockerId, @Param("blockedId") UUID blockedId);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void resetFailedAttempts(@Param("userId") UUID userId);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedAttempts(@Param("userId") UUID userId);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.lastActive = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateLastActive(@Param("userId") UUID userId);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.loginCount = u.loginCount + 1 WHERE u.id = :userId")
    void incrementLoginCount(@Param("userId") UUID userId);

    @Query("SELECT u FROM User u WHERE u.online = true")
    List<User> findOnlineUsers();

    // 1) مستخدمون لديهم جلسات غير نشطة قبل threshold
    // يعتمد على وجود entity UserSession في الـ JPA context
    @Query("SELECT DISTINCT s.user FROM UserSession s WHERE s.lastActivity < :threshold AND s.active = true")
    List<User> findUsersWithInactiveSessions(@Param("threshold") OffsetDateTime threshold);

    // 2) (مؤقت) مستخدمون الذين لم يسجلوا الدخول منذ وقت طويل
    // نستخدم lastLogin كبديل لـ passwordChangedAt إذا لم يكن موجودًا
    @Query("SELECT u FROM User u WHERE u.lastLogin IS NOT NULL AND u.lastLogin < :threshold")
    List<User> findUsersWithOldPasswords(@Param("threshold") OffsetDateTime threshold);

    // 3) مستخدمون لديهم محاولات فاشلة >= attempts
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :attempts")
    List<User> findUsersWithExcessiveFailedAttempts(@Param("attempts") int attempts);

    // بحث بالموبايل
    Optional<User> findByNormalizedPhone(String normalizedPhone);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.normalizedPhone = :normalizedPhone")
    Optional<User> findByNormalizedPhoneForUpdate(@Param("normalizedPhone") String normalizedPhone);

    boolean existsByNormalizedPhone(String normalizedPhone);

//    Optional<User> findByNormalizedPhone(String normalizedPhone);


}
