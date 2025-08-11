package com.nova.poneglyph.Auth.service;

import com.nova.poneglyph.Auth.dto.RegisterDto;
import com.nova.poneglyph.dto.AuthResponse;
import com.nova.poneglyph.mapper.UserMapper;
import com.nova.poneglyph.model.User;
import com.nova.poneglyph.service.RefreshTokenService;
import com.nova.poneglyph.service.UserTokenService;
import com.nova.poneglyph.user.exc.WrongCredentialsException;
import com.nova.poneglyph.user.request.LoginRequest;
import com.nova.poneglyph.user.request.RegisterRequest;
import com.nova.poneglyph.user.service.UserService;
import com.nova.poneglyph.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final UserService userService;
    private final UserTokenService userTokenService;
    private final RefreshTokenService refreshTokenService; // خدمة جديدة

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        if (auth.isAuthenticated()) {
            User user = userService.getUserByUsername(request.getUsername());

            // إنشاء التوكنين
            String accessToken = jwtUtils.generateAccessToken(user);
            String refreshToken = jwtUtils.generateRefreshToken(user);

            // حفظ التوكنين
            userTokenService.saveOrUpdateToken(user.getId(), accessToken, jwtUtils.getExpiration(accessToken));
            refreshTokenService.saveRefreshToken(user.getId(), refreshToken, jwtUtils.getRefreshTokenExpiration());

            return new AuthResponse(
                    accessToken,
                    refreshToken,
                    jwtUtils.getAccessTokenExpiration(),
                    UserMapper.toDTO(user, true)
            );
        } else {
            throw new WrongCredentialsException("بيانات الدخول غير صحيحة");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        // التحقق من صحة الـ refresh token
        if (!refreshTokenService.isValid(refreshToken)) {
            throw new WrongCredentialsException("Refresh token غير صالح");
        }

        String username = jwtUtils.extractUsername(refreshToken);
        User user = userService.getUserByUsername(username);

        // إنشاء توكنات جديدة
        String newAccessToken = jwtUtils.generateAccessToken(user);
        String newRefreshToken = jwtUtils.generateRefreshToken(user);

        // حفظ التوكنات الجديدة
        userTokenService.saveOrUpdateToken(user.getId(), newAccessToken, jwtUtils.getExpiration(newAccessToken));
        refreshTokenService.updateRefreshToken(refreshToken, newRefreshToken, jwtUtils.getRefreshTokenExpiration());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                jwtUtils.getAccessTokenExpiration(),
                UserMapper.toDTO(user, true)
        );
    }



    public RegisterDto register(RegisterRequest request) {


     User user= userService.saveUser(request);
        RegisterDto registerDto =new RegisterDto();

        registerDto.setUsername(user.getUsername());
        registerDto.setEmail(user.getEmail());
        registerDto.setId(user.getId());
        return registerDto;
    }

    public void logout(String token) {
        userTokenService.invalidateToken(token);
    }

}
