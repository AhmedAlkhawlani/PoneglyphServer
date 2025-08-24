//package com.nova.poneglyph.util;
//
//import com.nova.poneglyph.config.v2.CustomUserDetails;
//import com.nova.poneglyph.domain.user.User;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.time.Instant;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//import java.util.function.Function;
//
//@Component
//public class JwtUtil2 {
//
//    private static final Logger log = LoggerFactory.getLogger(JwtUtil2.class);
//
//    @Value("${jwt.secret}")
//    private String secret;
//
//    @Value("${jwt.access.expiration}")
//    private long accessExpiration;
//
//    @Value("${jwt.refresh.expiration}")
//    private long refreshExpiration;
//
//    private SecretKey signingKey;
//
//    @PostConstruct
//    private void init() {
//        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
//        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
//        log.info("JwtUtil initialized with secret length: {}", keyBytes.length);
//    }
//
//    private SecretKey getSigningKey() {
//        return signingKey;
//    }
//
//    public String generateAccessToken(User user) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("userId", user.getId().toString());
//        claims.put("phone", user.getPhoneNumber());
//        claims.put("type", "access");
//
//        Instant now = Instant.now();
//        String token = Jwts.builder()
//                .setClaims(claims)
//                .setSubject(user.getId().toString())
//                .setIssuedAt(Date.from(now))
//                .setExpiration(Date.from(now.plusSeconds(accessExpiration)))
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                .compact();
//
//        log.debug("Generated access token for userId {}: {}", user.getId(), token);
//        return token;
//    }
//
//    public String generateRefreshToken(User user) {
//        Map<String, Object> claims = new HashMap<>();
//        String jti = UUID.randomUUID().toString();
//        claims.put("userId", user.getId().toString());
//        claims.put("type", "refresh");
//
//        Instant now = Instant.now();
//        String token = Jwts.builder()
//                .setClaims(claims)
//                .setSubject(user.getId().toString())
//                .setId(jti)
//                .setIssuedAt(Date.from(now))
//                .setExpiration(Date.from(now.plusSeconds(refreshExpiration)))
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                .compact();
//
//        log.debug("Generated refresh token for userId {}: {}", user.getId(), token);
//        return token;
//    }
//
//    public String extractUserId(String token) {
//        String userId = extractClaim(token, Claims::getSubject);
//        log.debug("extractUserId: {}", userId);
//        return userId;
//    }
//
//    public String extractTokenType(String token) {
//        String type = extractClaim(token, claims -> claims.get("type", String.class));
//        log.debug("extractTokenType: {}", type);
//        return type;
//    }
//
//    public Date extractExpiration(String token) {
//        Date exp = extractClaim(token, Claims::getExpiration);
//        log.debug("extractExpiration: {}", exp);
//        return exp;
//    }
//
//    public String extractJti(String token) {
//        String jti = extractClaim(token, Claims::getId);
//        log.debug("extractJti: {}", jti);
//        return jti;
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        if (claims == null) {
//            log.warn("extractClaim: claims are null for token {}", token);
//            return null;
//        }
//        T value = claimsResolver.apply(claims);
//        log.debug("extractClaim value: {}", value);
//        return value;
//    }
//
//
//    private Claims extractAllClaims(String token) {
//        try {
//            JwtParser parser = Jwts.parser()
//                    .verifyWith(getSigningKey()) // المفتاح للتحقق من التوقيع
//                    .build();
////            Jws<Claims> jwsClaims = parser.parseClaimsJws(token);
////            log.debug("JWT parsed successfully. Claims: {}", jwsClaims.getBody());
//
//            return parser.parseSignedClaims(token).getPayload();
//        } catch (ExpiredJwtException e) {
//            // إذا انتهت الصلاحية نرجع الـ claims (مفيد لمعالجة الـ refresh أو عرض سبب الرفض)
//            log.warn("JWT token expired. Claims still available: {}", e.getClaims());
//
//            throw  e;
//        } catch (JwtException | IllegalArgumentException ex) {
//            // أي خطأ في البناء أو التوقيع أو الصيغة — نُعيد null أو نرمي استثناء مخصص وفق حاجتك
//            log.error("Failed to parse JWT token: {}. Error: {}", token, ex.getMessage(), ex);
//            throw new JwtException("Failed to parse JWT token: " + ex.getMessage(), ex);
//        }
//    }
//
//    private Boolean isTokenExpired(String token) {
//        Date exp = extractExpiration(token);
//        boolean expired = (exp == null) || exp.before(new Date());
//        log.debug("isTokenExpired: {} (exp: {})", expired, exp);
//        return expired;
//    }
//
//    public Boolean isTokenValid(String token, String expectedUserId) {
//        try {
//            final String userId = extractUserId(token);
//            final String tokenType = extractTokenType(token);
//            boolean valid = (userId != null && userId.equals(expectedUserId) &&
//                    !isTokenExpired(token) && "access".equals(tokenType));
//            log.debug("isTokenValid check: userId={}, expectedUserId={}, tokenType={}, expired={}, result={}",
//                    userId, expectedUserId, tokenType, isTokenExpired(token), valid);
//            return valid;
//        } catch (JwtException e) {
//            log.error("isTokenValid exception: {}", e.getMessage(), e);
//            return false;
//        }
//    }
//
//    public Boolean isTokenValid(String token, CustomUserDetails userDetails) {
//        if (userDetails == null) {
//            log.warn("isTokenValid: userDetails is null");
//            return false;
//        }
//        return isTokenValid(token, userDetails.getId().toString());
//    }
//
//    public Boolean isRefreshTokenValid(String token, String expectedUserId) {
//        try {
//            final String userId = extractUserId(token);
//            final String tokenType = extractTokenType(token);
//            boolean valid = (userId != null && userId.equals(expectedUserId) &&
//                    !isTokenExpired(token) && "refresh".equals(tokenType));
//            log.debug("isRefreshTokenValid check: userId={}, expectedUserId={}, tokenType={}, expired={}, result={}",
//                    userId, expectedUserId, tokenType, isTokenExpired(token), valid);
//            return valid;
//        } catch (JwtException e) {
//            log.error("isRefreshTokenValid exception: {}", e.getMessage(), e);
//            return false;
//        }
//    }
//
//    public String extractPhone(String token) {
//        String phone = extractClaim(token, claims -> claims.get("phone", String.class));
//        log.debug("extractPhone: {}", phone);
//        return phone;
//    }
//}
