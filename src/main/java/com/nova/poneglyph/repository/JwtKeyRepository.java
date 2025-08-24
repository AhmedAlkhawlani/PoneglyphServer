package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.auth.JwtKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface JwtKeyRepository extends JpaRepository<JwtKey, Long> {
    Optional<JwtKey> findTopByKeyTypeOrderByCreatedAtDesc(String keyType);
    Optional<JwtKey> findByKid(String kid);

    @Query("select k from JwtKey k where k.keyType = 'ARCHIVED' and (k.expiresAt is null or k.expiresAt > ?1)")
    List<JwtKey> findActiveArchived(OffsetDateTime now);

    @Modifying
    @Transactional
    int deleteByExpiresAtBefore(OffsetDateTime cutoff);
}
