package com.nova.poneglyph.util;

import com.nova.poneglyph.config.v2.KeyStorageService;
import com.nova.poneglyph.domain.user.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final KeyStorageService keyStorageService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    @Value("${jwt.issuer:nova-poneglyph}")
    private String jwtIssuer;

    @Value("${jwt.audience:mobile-app}")
    private String jwtAudience;

    private SecretKey signingKey;

    public JwtUtil(KeyStorageService keyStorageService) {
        this.keyStorageService = keyStorageService;
    }

    //    @PostConstruct
//    private void init() {
//        updateSigningKey(this.secret);
//        log.info("JwtUtil initialized with issuer: {}, audience: {}", jwtIssuer, jwtAudience);
//    }
@PostConstruct
private void init() {
    // لا نقوم بتهيئة المفتاح من application.properties فقط
    // بل نحاول تحميل المفتاح من التخزين المستدام
    try {
        String persistedSecret = keyStorageService.getCurrentJwtSecret();
        if (persistedSecret != null) {
            updateSigningKey(persistedSecret);
            log.info("JwtUtil initialized with persisted key");
        } else {
            // إذا لم يوجد مفتاح مستدام، استخدام المفتاح من الإعدادات
            updateSigningKey(this.secret);
            keyStorageService.storeCurrentJwtSecret(this.secret);
            log.info("JwtUtil initialized with new key and persisted it");
        }
    } catch (Exception e) {
        log.error("Failed to initialize with persisted key, using config key", e);
        updateSigningKey(this.secret);
    }
}

    public void updateSigningKey(String newSecret) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(newSecret);
        } catch (IllegalArgumentException ex) {
            keyBytes = newSecret.getBytes(StandardCharsets.UTF_8);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT signing key updated (key bytes length={})", keyBytes.length);
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    // ===================== TOKEN GENERATION =====================
    public String generateAccessToken(User user) {
        return generateToken(user, "access", accessExpiration);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, "refresh", refreshExpiration);
    }

    private String generateToken(User user, String type, long expirationInSeconds) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("type", type);
        claims.put("phone", user.getPhoneNumber());

        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setId(jti)
                .setIssuer(jwtIssuer)
                .setAudience(jwtAudience)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationInSeconds)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        log.debug("Generated {} token for userId {} with jti={}", type, user.getId(), jti);
        return token;
    }

    // ===================== CLAIM EXTRACTION =====================
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (JwtException e) {
            log.debug("Failed to extract claim: {}", e.getMessage());
            return null;
        }
    }

    /**
     * يحاول parse الـ JWT بالتحقق من التوقيع.
     * لا يسجل SignatureException كـ ERROR لتقليل الضوضاء (يمكن أن يحدث أثناء تدوير المفاتيح).
     */
    private Claims extractAllClaims(String token) {
        try {
            JwtParser parser;
            try {
                parser = Jwts.parser()
                        .setSigningKey(getSigningKey())
                        .setAllowedClockSkewSeconds(60)
                        .build();
            } catch (NoSuchMethodError nsme) {
                // fallback لو كانت نسخة المكتبة قديمة
                parser = Jwts.parser().setSigningKey(getSigningKey()).build();
            }
            return parser.parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired. Claims may still be available.");
            throw e;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            // توقيع غير مطابق — متوقع أحياناً (توكن قديم بعد تدوير مفاتيح)
            log.debug("JWT signature invalid (possible old/rotated key): {}", e.getMessage());
            throw e;
        } catch (JwtException | IllegalArgumentException ex) {
            // أخطاء عامة أخرى — سجلها كـ debug لأننا غالبًا نتعامل معها في منطق أعلى
            log.debug("Failed to parse JWT token: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * استخراج jti بطريقة آمنة:
     * 1) أولاً نحاول فك payload (Base64Url) واستخراج الحقل "jti" بدون تحقق التوقيع
     *    — هذا يمنع SignatureException من الحدوث عندما نحتاج jti فقط للـ lookup.
     * 2) إذا فشل ذلك نحاول parse موثوق عبر extractClaim().
     */
    public String extractJti(String token) {
        // Attempt 1: decode payload (fast, no signature verification)
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payloadB64 = parts[1];
                byte[] decoded = Base64.getUrlDecoder().decode(payloadB64);
                String payloadJson = new String(decoded, StandardCharsets.UTF_8);
                JsonNode node = MAPPER.readTree(payloadJson);
                JsonNode jtiNode = node.get("jti");
                if (jtiNode != null && !jtiNode.isNull()) {
                    String jti = jtiNode.asText();
                    if (jti != null && !jti.isBlank()) {
                        return jti;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("extractJti: payload decode failed: {}", e.getMessage());
        }

        // Attempt 2: secured parse (may throw SignatureException)
        try {
            return extractClaim(token, Claims::getId);
        } catch (JwtException e) {
            log.debug("extractJti: parse with signature failed: {}", e.getMessage());
            return null;
        }
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public String extractPhone(String token) {
        return extractClaim(token, claims -> claims.get("phone", String.class));
    }

    public String extractIssuer(String token) {
        return extractClaim(token, Claims::getIssuer);
    }

    public String extractAudience(String token) {
        try {
            Claims claims = extractAllClaims(token);

            Object audRaw = claims.get("aud");
            if (audRaw instanceof String) {
                return (String) audRaw;
            } else if (audRaw instanceof java.util.Collection) {
                java.util.Collection<?> col = (java.util.Collection<?>) audRaw;
                if (!col.isEmpty()) return col.iterator().next().toString();
            }

            try {
                Object maybe = claims.getAudience();
                if (maybe instanceof String) return (String) maybe;
                if (maybe instanceof java.util.Collection) {
                    java.util.Collection<?> c = (java.util.Collection<?>) maybe;
                    if (!c.isEmpty()) return c.iterator().next().toString();
                }
            } catch (Throwable ignored) {}

            return null;
        } catch (JwtException e) {
            log.debug("Failed to extract audience claim: {}", e.getMessage());
            return null;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ===================== TOKEN VALIDATION =====================
    private boolean isTokenExpired(String token) {
        Date exp = extractExpiration(token);
        return exp == null || exp.before(new Date());
    }

    public boolean isTokenValid(String token, String expectedUserId) {
        try {
            String userId = extractUserId(token);
            String type = extractTokenType(token);
            String issuer = extractIssuer(token);
            String audience = extractAudience(token);

            boolean valid = userId != null
                    && userId.equals(expectedUserId)
                    && "access".equals(type)
                    && jwtIssuer.equals(issuer)
                    && jwtAudience.equals(audience)
                    && !isTokenExpired(token);

            log.debug("Token validation result for user {}: {}", expectedUserId, valid);
            return valid;
        } catch (JwtException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        if (userDetails == null) return false;
        return isTokenValid(token, userDetails.getUsername());
    }

    public boolean isRefreshTokenValid(String token, String expectedUserId) {
        try {
            String userId = extractUserId(token);
            String type = extractTokenType(token);
            String issuer = extractIssuer(token);
            String audience = extractAudience(token);

            boolean valid = userId != null
                    && userId.equals(expectedUserId)
                    && "refresh".equals(type)
                    && jwtIssuer.equals(issuer)
                    && jwtAudience.equals(audience)
                    && !isTokenExpired(token);

            log.debug("Refresh token validation result for user {}: {}", expectedUserId, valid);
            return valid;
        } catch (JwtException e) {
            log.debug("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateTokenWithKey(String token, String expectedUserId, String secretKey) {
        try {
            byte[] keyBytes;
            try {
                keyBytes = Base64.getDecoder().decode(secretKey);
            } catch (IllegalArgumentException ex) {
                keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            }
            SecretKey tempSigningKey = Keys.hmacShaKeyFor(keyBytes);

            JwtParser parser;
            try {
                parser = Jwts.parser()
                        .setSigningKey(tempSigningKey)
                        .setAllowedClockSkewSeconds(60)
                        .build();
            } catch (NoSuchMethodError nsme) {
                parser = Jwts.parser().setSigningKey(tempSigningKey).build();
            }

            Claims claims = parser.parseClaimsJws(token).getBody();

            String userId = claims.getSubject();
            String type = claims.get("type", String.class);
            String issuer = claims.getIssuer();

            String audience = null;
            Object audObj = claims.get("aud");
            if (audObj instanceof String) {
                audience = (String) audObj;
            } else if (audObj instanceof java.util.Collection) {
                java.util.Collection<?> c = (java.util.Collection<?>) audObj;
                if (!c.isEmpty()) audience = c.iterator().next().toString();
            } else {
                try {
                    Object maybe = claims.getAudience();
                    if (maybe instanceof String) audience = (String) maybe;
                    if (maybe instanceof java.util.Collection) {
                        java.util.Collection<?> c = (java.util.Collection<?>) maybe;
                        if (!c.isEmpty()) audience = c.iterator().next().toString();
                    }
                } catch (Throwable ignored) {}
            }

            Date expiration = claims.getExpiration();

            boolean valid = userId != null
                    && userId.equals(expectedUserId)
                    && "access".equals(type)
                    && jwtIssuer.equals(issuer)
                    && jwtAudience.equals(audience)
                    && expiration != null
                    && expiration.after(new Date());

            log.debug("Custom key validation result for user {}: {}", expectedUserId, valid);
            return valid;
        } catch (JwtException e) {
            log.debug("Token validation with custom key failed: {}", e.getMessage());
            return false;
        }
    }

    // في class JwtUtil نضيف هذه الطرق:

    public boolean isTokenBlacklisted(String token) {
        // سنعتمد على TokenBlacklistService بدلاً من التنفيذ المباشر هنا
        throw new UnsupportedOperationException("Use TokenBlacklistService instead");
    }

    public boolean validateToken(String token) {
        try {
            String userId = extractUserId(token);
            if (userId == null) return false;

            return isTokenValid(token, userId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token, String expectedUserId) {
        return isTokenValid(token, expectedUserId);
    }
}
