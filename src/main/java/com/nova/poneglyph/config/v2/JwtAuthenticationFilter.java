
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
import io.jsonwebtoken.JwtException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
//    private final UserDetailsService userDetailsService;

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
            String userId = jwtUtil.extractUserId(jwt); // يمكن يرمي ExpiredJwtException
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                String tokenType = jwtUtil.extractTokenType(jwt);
                if ("access".equalsIgnoreCase(tokenType) && jwtUtil.isTokenValid(jwt, (CustomUserDetails) userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
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
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT for {}: {}", request.getRequestURI(), ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"invalid_token\",\"message\":\"JWT invalid.\"}");
        } catch (Exception ex) {
            log.error("Unexpected JWT processing error", ex);
            // امُرّ للـ chain أو أرجع 500 حسب تفضيلك
            filterChain.doFilter(request, response);
        }
    }

}
