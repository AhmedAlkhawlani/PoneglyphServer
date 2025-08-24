package com.nova.poneglyph.config.v2;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtKeyInitializer {
    private static final Logger log = LoggerFactory.getLogger(JwtKeyInitializer.class);
    private final KeyStorageService keyStorageService;

    @PostConstruct
    public void ensureKey() {
        if (keyStorageService.getCurrentKey().isEmpty()) {
            keyStorageService.rotateAndGetNewCurrent(3072); // or 4096
            log.info("Generated initial CURRENT JWT RSA key");
        }
    }
}
