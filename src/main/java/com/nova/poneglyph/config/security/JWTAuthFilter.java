//
//
//package com.nova.poneglyph.config.security;
//
//import com.nova.poneglyph.utils.JWTUtils;
//import io.jsonwebtoken.ExpiredJwtException;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class JWTAuthFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private JWTUtils jwtUtils;
//
//    @Autowired
//    private CustomUserDetailsService customUserDetailsService;
//
//    @Autowired
//    private UserTokenService userTokenService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        final String authHeader = request.getHeader("Authorization");
//        final String jwtToken;
//        final String userEmail;
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        jwtToken = authHeader.substring(7); // إزالة "Bearer "
//
//        try {
//            userEmail = jwtUtils.extractUsername(jwtToken);
//
//            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);
//                String userId = ((User) userDetails).getId();
//
//                // ✅ التحقق من أن التوكن هو الأحدث للمستخدم
//                String currentToken = userTokenService.getCurrentTokenByUserId(userId);
//                if (!jwtToken.equals(currentToken)) {
//                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
//                            "تم تسجيل الدخول من جهاز آخر. يرجى تسجيل الدخول مرة أخرى.");
//                    return;
//                }
//
//                // التحقق من صحة Access Token فقط
//                if (jwtUtils.isValidToken(jwtToken, userDetails) &&
//                        userTokenService.isTokenValid(jwtToken)) {
//
//                    UsernamePasswordAuthenticationToken authToken =
//                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                    SecurityContextHolder.getContext().setAuthentication(authToken);
//                }
////                if (jwtUtils.isValidToken(jwtToken, userDetails)) {
////                    UsernamePasswordAuthenticationToken authToken =
////                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
////                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
////                    SecurityContextHolder.getContext().setAuthentication(authToken);
////                }
//            }
//        } catch (ExpiredJwtException ex) {
//            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
//                    "انتهت صلاحية الجلسة. يرجى استخدام /refresh-token للحصول على توكن جديد.");
//            return;
//        } catch (Exception ex) {
//            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                    "خطأ في المصادقة. يرجى المحاولة لاحقًا.");
//            return;
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
//        response.setStatus(status);
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//        response.getWriter().write("{\"error\": \"" + message + "\"}");
//    }
//}
