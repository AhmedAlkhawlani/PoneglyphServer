package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.auth.JwtKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface JwtKeyRepository extends JpaRepository<JwtKey, Long> {
    Optional<JwtKey> findTopByKeyTypeOrderByCreatedAtDesc(String keyType);

    List<JwtKey> findByKeyTypeAndExpiresAtAfter(String keyType, OffsetDateTime date);

    @Modifying
    @Query("DELETE FROM JwtKey j WHERE j.expiresAt < :date")
    void deleteByExpiresAtBefore(@Param("date") OffsetDateTime date);

    // استعلام إضافي للبحث بالمفتاح
    Optional<JwtKey> findBySecret(String secret);
}
