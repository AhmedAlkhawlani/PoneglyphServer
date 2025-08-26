package com.nova.poneglyph.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.poneglyph.config.v2.KeyStorageService;
import com.nova.poneglyph.domain.user.User;
import io.jsonwebtoken.*;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// not used with RSA but kept if needed
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KeyStorageService keys;

    @Value("${jwt.issuer:nova-poneglyph}") private String issuer;
    @Value("${jwt.audience:mobile-app}") private String audience;
    @Value("${jwt.access.expiration:1800}") private long accessExpSec;
    @Value("${jwt.refresh.expiration:1209600}") private long refreshExpSec;
    @Value("${jwt.refresh.expired_grace_seconds:300}") private long refreshGraceSec; // allow parsing recently expired for rotation

    public JwtUtil(KeyStorageService keys) { this.keys = keys; }

    /* =================== Generate =================== */
    public String generateAccessToken(User user) { return generate(user, "access", accessExpSec); }
    public String generateRefreshToken(User user) { return generate(user, "refresh", refreshExpSec); }

    private String generate(User user, String type, long expSeconds) {
        var currentOpt = keys.getCurrentKey();
        if (currentOpt.isEmpty()) throw new IllegalStateException("No CURRENT JWT key available");
        var current = currentOpt.get();
        RSAPrivateKey priv = keys.toPrivateKey(current);

        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setHeaderParam("kid", current.getKid())
                .claim("type", type)
                .claim("userId", user.getId().toString())
                .claim("phone", user.getPhoneNumber())
                .setIssuer(issuer)
                .setAudience(audience)
                .setSubject(user.getId().toString())
                .setId(jti)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith(priv, SignatureAlgorithm.RS256)
                .compact();
    }

    /* =================== Extract =================== */
    public <T> T extractClaim(String token, Function<Claims, T> resolver, boolean allowExpired) {
        try {
            Claims claims = parseClaims(token, allowExpired);
            return resolver.apply(claims);
        } catch (JwtException e) {
            log.debug("extractClaim failed: {}", e.getMessage());
            return null;
        }
    }



    private Claims parseClaims(String token, boolean allowExpired) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new MalformedJwtException("JWT must have 3 parts");
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            Map<String, Object> header = MAPPER.readValue(headerJson, Map.class);
            String kid = (String) header.get("kid");

            RSAPublicKey pub = resolveVerificationKey(token, kid);
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(pub)
                    .build();
            return parser.parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException eje) {
            if (allowExpired) return eje.getClaims();
            throw eje;
        } catch (Exception e) {
            throw new JwtException("Failed to parse claims", e);
        }
    }
    private RSAPublicKey resolveVerificationKey(String token, String kid) {
        // Try by kid current/archived
        if (kid != null) {
            return keys.getKeyByKid(kid)
                    .map(keys::toPublicKey)
                    .or(() -> keys.getActiveArchivedKeys().stream()
                            .filter(k -> kid.equals(k.getKid()))
                            .findFirst()
                            .map(keys::toPublicKey))
                    .orElseGet(() -> tryAllPublicKeys(token));
        }
        // No kid: try all known public keys (current + active archived)
        return tryAllPublicKeys(token);
    }

    private RSAPublicKey tryAllPublicKeys(String token) {
        var all = new ArrayList<>(keys.getActiveArchivedKeys());
        keys.getCurrentKey().ifPresent(all::add);
        for (var k : all) {
            try {
                RSAPublicKey pk = keys.toPublicKey(k);
                Jwts.parserBuilder().setSigningKey(pk).build().parseClaimsJws(token);
                return pk; // first that verifies
            } catch (Exception ignore) { }
        }
        throw new SignatureException("No matching key found for token");
    }

    public String extractJti(String token) { return extractClaim(token, Claims::getId, true); }
    public String extractUserId(String token) { return extractClaim(token, Claims::getSubject, true); }
    public String extractTokenType(String token) { return extractClaim(token, c -> c.get("type", String.class), true); }
    public Date extractExpiration(String token) { return extractClaim(token, Claims::getExpiration, true); }
    public String extractIssuer(String token) { return extractClaim(token, Claims::getIssuer, true); }
    public String extractAudience(String token) {
        return extractClaim(token, c -> {
            Object aud = c.get("aud");
            if (aud instanceof String s) return s;
            if (aud instanceof Collection<?> c2 && !c2.isEmpty()) return String.valueOf(c2.iterator().next());
            try { return c.getAudience(); } catch (Throwable t) { return null; }
        }, true);
    }

    /* =================== Validate =================== */
    private boolean coreValidate(String token, String expectedUserId, String expectedType, boolean allowExpired) {
        try {
            Claims c = parseClaims(token, allowExpired);
            boolean expOk = allowExpired || c.getExpiration().after(new Date());
            String aud = extractAudience(token);
            return expOk
                    && expectedUserId.equals(c.getSubject())
                    && expectedType.equalsIgnoreCase(String.valueOf(c.get("type")))
                    && issuer.equals(c.getIssuer())
                    && audience.equals(aud);
        } catch (JwtException e) {
            log.debug("validate failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateAccessToken(String token, String expectedUserId) {
        return coreValidate(token, expectedUserId, "access", false);
    }

    /**
     * Refresh token may be *recently* expired. Allow parsing expired claims but enforce grace.
     */
    public boolean validateRefreshToken(String token, String expectedUserId) {
        try {
            Claims c = parseClaims(token, true);
            boolean typeOk = "refresh".equalsIgnoreCase(String.valueOf(c.get("type")));
            boolean baseOk = expectedUserId.equals(c.getSubject()) && issuer.equals(c.getIssuer());
            boolean audOk = audience.equals(extractAudience(token));
            boolean timeOk;
            if (c.getExpiration() == null) return false;
            Date now = new Date();
            if (c.getExpiration().after(now)) {
                timeOk = true;
            } else {
                long diff = (now.getTime() - c.getExpiration().getTime()) / 1000L;
                timeOk = diff <= refreshGraceSec; // within grace window
            }
            return typeOk && baseOk && audOk && timeOk;
        } catch (JwtException e) {
            log.debug("refresh validate failed: {}", e.getMessage());
            return false;
        }
    }
}
