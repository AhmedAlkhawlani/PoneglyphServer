package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.moderation.SystemBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;




import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SystemBanRepository extends JpaRepository<SystemBan, Long> {

    @Query("SELECT b FROM SystemBan b WHERE b.normalizedPhone = :phone AND b.active = true " +
            "ORDER BY b.createdAt DESC LIMIT 1")
    Optional<SystemBan> findFirstByNormalizedPhoneAndActiveIsTrueOrderByCreatedAtDesc(
            @Param("phone") String normalizedPhone);

    @Query("SELECT COUNT(b) FROM SystemBan b WHERE b.normalizedPhone = :phone AND b.active = true")
    long countActiveBansByPhone(@Param("phone") String normalizedPhone);

    @Query("UPDATE SystemBan b SET b.active = false WHERE b.normalizedPhone = :phone")
    void deactivateBansByPhone(@Param("phone") String normalizedPhone);
}
