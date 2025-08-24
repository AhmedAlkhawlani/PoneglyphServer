package com.nova.poneglyph.config.v2;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public void addToBlacklist(String token, long expirationInSeconds) {
        String jti = extractJtiFromToken(token);
        if (jti != null) {
            redisTemplate.opsForValue().set("jwt:blacklist:" + jti,
                    "blacklisted", expirationInSeconds, TimeUnit.SECONDS);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String jti = extractJtiFromToken(token);
        if (jti == null) return false;

        return redisTemplate.hasKey("jwt:blacklist:" + jti);
    }

    private String extractJtiFromToken(String token) {
        try {
            // استخراج jti من التوكن بدون التحقق من التوقيع
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                com.fasterxml.jackson.databind.JsonNode node =
                        new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload);
                return node.get("jti").asText();
            }
        } catch (Exception e) {
            // Logger here if needed
        }
        return null;
    }
}
