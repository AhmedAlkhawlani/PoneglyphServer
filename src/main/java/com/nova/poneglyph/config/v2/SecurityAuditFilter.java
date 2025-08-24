
package com.nova.poneglyph.config.v2;

import com.nova.poneglyph.service.audit.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class SecurityAuditFilter extends OncePerRequestFilter {

    private final AuditService auditService;

    public SecurityAuditFilter(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // تسجيل معلومات الطلب
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = request.getRemoteAddr();

        // متابعة معالجة الطلب
        filterChain.doFilter(request, response);

        // تسجيل معلومات الاستجابة
        int status = response.getStatus();

        // تسجيل حدث التدقيق
        if (shouldAuditRequest(requestUri)) {
            UUID actorId = getCurrentUserId(); // استخدام الدالة المعدلة
            String action = method + " " + requestUri;

            auditService.logEvent(
                    actorId,
                    "HTTP_REQUEST",
                    "endpoint",
                    requestUri,
                    Map.of(
                            "method", method,
                            "status", status,
                            "ip", remoteAddr,
                            "user_agent", request.getHeader("User-Agent")
                    )
            );
        }
    }

    private boolean shouldAuditRequest(String requestUri) {
        // استبعاد بعض المسارات من التدقيق
        return !requestUri.startsWith("/actuator") &&
                !requestUri.startsWith("/swagger") &&
                !requestUri.startsWith("/v3/api-docs") &&
                !requestUri.contains("/auth/"); // استبعاد مسارات المصادقة
    }

    private UUID getCurrentUserId() {
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            // تجنب تحويل "anonymousUser" إلى UUID
            if ("anonymousUser".equals(username)) {
                return null;
            }

            try {
                return UUID.fromString(username);
            } catch (IllegalArgumentException e) {
                // إذا كان الاسم ليس UUID صالحاً، ترجع null
                return null;
            }
        }
        return null;
    }
}
