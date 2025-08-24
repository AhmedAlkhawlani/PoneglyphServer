package com.nova.poneglyph.config.v2;

import com.nova.poneglyph.service.auth.CustomUserDetailsService;

import com.nova.poneglyph.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;


// ... imports ...

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final KeyStorageService keyStorageService; // may be used for diagnostics
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // 1) Quick blacklist check
            if (tokenBlacklistService != null && tokenBlacklistService.isTokenBlacklisted(jwt)) {
                log.warn("Blacklisted token attempt for request {}", request.getRequestURI());
                unauthorizedJson(response, HttpServletResponse.SC_UNAUTHORIZED, "token_revoked", "Token has been revoked.");
                return;
            }

            // 2) Extract userId (subject) without failing on expiry
            String userId = jwtUtil.extractUserId(jwt);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 3) Validate as access token (strict)
                boolean valid = jwtUtil.validateAccessToken(jwt, userId);

                if (valid) {
                    // load user and populate security context
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authentication successful for user: {} on {}", userId, request.getRequestURI());

                    filterChain.doFilter(request, response);
                    return;
                }

                // 4) Not valid — determine if it's expired or simply invalid
                Date exp = jwtUtil.extractExpiration(jwt);
                if (exp != null && exp.before(new Date())) {
                    log.debug("Expired access token for user {} on {}", userId, request.getRequestURI());
                    unauthorizedJson(response, HttpServletResponse.SC_UNAUTHORIZED, "token_expired", "Access token expired. Use refresh token.");
                    return;
                } else {
                    log.debug("Invalid access token for user {} on {}", userId, request.getRequestURI());
                    unauthorizedJson(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid_token", "JWT invalid.");
                    return;
                }
            }

            // If no userId or authentication already present, continue chain
            filterChain.doFilter(request, response);

        } catch (io.jsonwebtoken.ExpiredJwtException eje) {
            log.warn("Expired JWT for {}: {}", request.getRequestURI(), eje.getMessage());
            unauthorizedJson(response, HttpServletResponse.SC_UNAUTHORIZED, "token_expired", "Access token expired. Use refresh token.");
        } catch (Exception ex) {
            log.warn("JWT validation error for {}: {}", request.getRequestURI(), ex.getMessage());
            unauthorizedJson(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid_token", "JWT invalid.");
        }
    }

    private void unauthorizedJson(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = String.format("{\"error\":\"%s\",\"message\":\"%s\"}", escapeJson(error), escapeJson(message));
        response.getWriter().write(body);
    }

    // minimal JSON escape for simple messages
    private String escapeJson(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20 || c > 0x7E) {
                        // أي محارف غير قابلة للطباعة → نرجعها بصيغة Unicode
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

}
