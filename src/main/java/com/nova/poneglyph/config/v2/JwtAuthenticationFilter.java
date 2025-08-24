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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


// ... imports ...

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final KeyStorageService keyStorageService;
    private final TokenBlacklistService tokenBlacklistService; // أضفنا هذا

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
            // التحقق من أن التوكن ليس في القائمة السوداء
            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                log.warn("Blacklisted token attempted: {}", jwtUtil.extractJti(jwt));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"token_revoked\",\"message\":\"Token has been revoked.\"}");
                return;
            }

            String userId = jwtUtil.extractUserId(jwt);
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                boolean isValid = jwtUtil.validateToken(jwt, userId);

                if (!isValid) {
                    isValid = validateWithArchivedKeys(jwt, userId);
                }

                if (isValid) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                    String tokenType = jwtUtil.extractTokenType(jwt);

                    if ("access".equalsIgnoreCase(tokenType)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("Authentication successful for user: {}", userId);
                    } else {
                        log.debug("JWT is not an access token for user: {}", userId);
                    }
                } else {
                    log.debug("JWT is not valid for user: {}", userId);
                }
            }
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException eje) {
            log.warn("Expired JWT for {}: {}", request.getRequestURI(), eje.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"token_expired\",\"message\":\"Access token expired. Use refresh token.\"}");
        } catch (Exception ex) {
            log.warn("Invalid JWT for {}: {}", request.getRequestURI(), ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"invalid_token\",\"message\":\"JWT invalid.\"}");
        }
    }

    private boolean validateWithArchivedKeys(String token, String expectedUserId) {
        java.util.List<String> archivedSecrets = keyStorageService.getArchivedJwtSecrets();

        for (String secret : archivedSecrets) {
            try {
                if (jwtUtil.validateTokenWithKey(token, expectedUserId, secret)) {
                    log.info("Token validated using archived key for user: {}", expectedUserId);
                    return true;
                }
            } catch (Exception e) {
                log.debug("Failed to validate with archived key: {}", e.getMessage());
            }
        }

        return false;
    }
}
