package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.auth.OtpCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
//    @Query("SELECT o FROM OtpCode o WHERE o.normalizedPhone = :phone AND o.expiresAt > :now AND o.used = false ORDER BY o.expiresAt DESC LIMIT 1")
//    Optional<OtpCode> findLatestValidOtp(@Param("phone") String phone, @Param("now") OffsetDateTime now);
    @Query("select o from OtpCode o where o.normalizedPhone = :norm and o.used = false and o.expiresAt > :now order by o.createdAt desc LIMIT 1")
    Optional<OtpCode> findLatestValidOtp(@Param("norm") String normalizedPhone, @Param("now") OffsetDateTime now);
    List<OtpCode> findByNormalizedPhoneOrderByCreatedAtDesc(String normalizedPhone);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OtpCode o where o.id = :id")
    Optional<OtpCode> findByIdForUpdate(@Param("id") Long id);
}
