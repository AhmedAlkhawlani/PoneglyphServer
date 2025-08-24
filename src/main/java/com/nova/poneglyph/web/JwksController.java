package com.nova.poneglyph.web;

import com.nova.poneglyph.config.v2.KeyStorageService;
import com.nova.poneglyph.domain.auth.JwtKey;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class JwksController {
    private final KeyStorageService keys;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public String jwks() {
        return keys.getCachedJwksJson().orElseGet(() -> {
            List<Map<String, Object>> jwks = new ArrayList<>();
            List<JwtKey> entries = new ArrayList<>(keys.getActiveArchivedKeys());
            keys.getCurrentKey().ifPresent(entries::add);
            for (JwtKey k : entries) {
                RSAPublicKey pub = keys.toPublicKey(k);
                Map<String, Object> jwk = new LinkedHashMap<>();
                jwk.put("kty", "RSA");
                jwk.put("kid", k.getKid());
                jwk.put("alg", k.getAlg());
                jwk.put("use", "sig");
                jwk.put("n", base64Url(pub.getModulus()));
                jwk.put("e", base64Url(pub.getPublicExponent()));
                jwks.add(jwk);
            }
            String json = "{\"keys\":" + toJson(jwks) + "}";
            keys.cacheJwksJson(json, 60); // cache 60s in Redis
            return json;
        });
    }

    private static String toJson(Object o) {
        try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(o); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String base64Url(BigInteger v) {
        byte[] bytes = v.toByteArray();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
