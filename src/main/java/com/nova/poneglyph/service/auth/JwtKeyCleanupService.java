package com.nova.poneglyph.service.auth;

import com.nova.poneglyph.repository.JwtKeyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class JwtKeyCleanupService {
    private static final Logger log = LoggerFactory.getLogger(JwtKeyCleanupService.class);

    private final JwtKeyRepository jwtKeyRepository;

    @Scheduled(cron = "0 0 3 * * ?") // كل يوم الساعة 3 صباحاً
    @Transactional
    public void cleanupExpiredJwtKeys() {
        try {
//            int deletedCount =
                    jwtKeyRepository.deleteByExpiresAtBefore(OffsetDateTime.now());
            log.info("Cleaned up {} expired JWT keys from database");
        } catch (Exception e) {
            log.error("Failed to cleanup expired JWT keys: {}", e.getMessage(), e);
        }
    }
}
