package com.nova.poneglyph.service;

import com.nova.poneglyph.model.RefreshToken;
import com.nova.poneglyph.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(String userId, String token, long expiration) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .orElse(new RefreshToken(userId));

        refreshToken.setToken(token);
        refreshToken.setExpiration(new Date(System.currentTimeMillis() + expiration));
        refreshTokenRepository.save(refreshToken);
    }

    public boolean isValid(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(t -> !t.isRevoked() && !t.getExpiration().before(new Date()))
                .orElse(false);
    }

    public void updateRefreshToken(String oldToken, String newToken, long expiration) {
        refreshTokenRepository.findByToken(oldToken).ifPresent(token -> {
            token.setToken(newToken);
            token.setExpiration(new Date(System.currentTimeMillis() + expiration));
            token.setRevoked(false);
            refreshTokenRepository.save(token);
        });
    }

    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }
}
