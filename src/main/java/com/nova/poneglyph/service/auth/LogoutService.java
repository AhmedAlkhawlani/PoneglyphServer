package com.nova.poneglyph.service.auth;

import com.nova.poneglyph.config.v2.TokenBlacklistService;
import com.nova.poneglyph.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    public void logout(String token) {
        // إضافة التوكن إلى القائمة السوداء حتى انتهاء صلاحيته
        Date expiration = jwtUtil.extractExpiration(token);
        long ttl = expiration.getTime() - System.currentTimeMillis();

        if (ttl > 0) {
            tokenBlacklistService.addToBlacklist(token, ttl / 1000);
        }
    }
}
