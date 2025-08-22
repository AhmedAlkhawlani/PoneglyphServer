package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByJti(UUID jti);

    @Query("select rt from RefreshToken rt where rt.session.id = :sessionId")
    Optional<RefreshToken> findBySessionId(@Param("sessionId") UUID sessionId);

//    @Modifying
//    @Query("update RefreshToken rt set rt.revokedAt = current_timestamp where rt.user.id = :userId and rt.revokedAt is null")
//    void revokeAllForUser(@Param("userId") UUID userId);
//
//    @Modifying
//    @Query("UPDATE RefreshToken rt SET rt.revokedAt = current_timestamp " +
//            "WHERE rt.user.id = :userId AND rt.jti <> :exceptJti AND rt.revokedAt IS NULL")
//    void revokeAllForUserExcept(@Param("userId") UUID userId, @Param("exceptJti") UUID exceptJti);

    @Modifying
    @Query("update RefreshToken rt set rt.revokedAt = current_timestamp where rt.user.id = :userId and rt.revokedAt is null")
    void revokeAllForUser(@Param("userId") UUID userId);

//    @Modifying
//    @Query("UPDATE RefreshToken rt SET rt.revokedAt = current_timestamp " +
//            "WHERE rt.user.id = :userId AND rt.jti <> :exceptJti AND rt.revokedAt IS NULL")
//    void revokeAllForUserExcept(@Param("userId") UUID userId, @Param("exceptJti") UUID exceptJti);


    // تحديث الاستعلام ليشمل العلاقة مع الجلسة
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = current_timestamp " +
            "WHERE rt.user.id = :userId AND rt.jti <> :exceptJti AND rt.revokedAt IS NULL")
    void revokeAllForUserExcept(@Param("userId") UUID userId, @Param("exceptJti") UUID exceptJti);
}

