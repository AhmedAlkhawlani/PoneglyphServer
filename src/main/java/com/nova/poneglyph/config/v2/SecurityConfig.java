
package com.nova.poneglyph.config.v2;

import com.nova.poneglyph.service.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    private final AuditService auditService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/otp/**",
                                "/api/auth/token/**",
                                "/api/auth/logout",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/ws/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/sessions/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // ترتيب الفلاتر: أولاً JwtAuth ثم audit (أو العكس حسب رغبتك)
                // غير ترتيب الفلاتر: JwtAuth أولاً ثم Audit
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(securityAuditFilter(), JwtAuthenticationFilter.class); // تغيير هنا


        return http.build();
    }

    @Bean
    public SecurityAuditFilter securityAuditFilter() {
        return new SecurityAuditFilter(auditService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // لا تترك "*" في الإنتاج — استخدم قائمة ثابتة أو بيئة
//         config.setAllowedOrigins(List.of(
//        "https://your-frontend.example.com",
//        "http://localhost:3000", // للتطوير المحلي
//        "http://127.0.0.1:3000"
//    ));
        config.setAllowedOrigins(List.of("https://your-frontend.example.com")); // عدّل هنا
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // مفيد لو التطبيق خلف proxy (load balancer) لثقة X-Forwarded-* headers
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
