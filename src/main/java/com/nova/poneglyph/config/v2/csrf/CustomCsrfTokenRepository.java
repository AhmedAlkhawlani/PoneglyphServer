package com.nova.poneglyph.config.v2.csrf;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CustomCsrfTokenRepository implements CsrfTokenRepository {

    private static final String CSRF_TOKEN_HEADER_NAME = "X-CSRF-TOKEN";
    private static final String CSRF_PARAMETER_NAME = "_csrf";

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        String token = UUID.randomUUID().toString();
        return new DefaultCsrfToken(CSRF_TOKEN_HEADER_NAME, CSRF_PARAMETER_NAME, token);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token != null) {
            response.setHeader(CSRF_TOKEN_HEADER_NAME, token.getToken());
        } else {
            response.setHeader(CSRF_TOKEN_HEADER_NAME, "");
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        String token = request.getHeader(CSRF_TOKEN_HEADER_NAME);
        if (token != null && !token.isEmpty()) {
            return new DefaultCsrfToken(CSRF_TOKEN_HEADER_NAME, CSRF_PARAMETER_NAME, token);
        }
        return null;
    }
}
