
package com.nova.poneglyph.config.v2;

import com.nova.poneglyph.config.v2.csrf.CustomCsrfTokenRepository;
import com.nova.poneglyph.service.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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
    private final CustomCsrfTokenRepository csrfTokenRepository;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .ignoringRequestMatchers(
                                "/api/auth/otp/**",
                                "/api/auth/token/**",
                                "/api/auth/sessions/**", // إضافة هذا الخط الهام
                                "/api/auth/logout",
                                "/ws/**",
                                "/api/contacts/**",
                                "/api/user/**",
                                "/ws-native/**"
                        ) // استثناء بعض المسارات من CSRF
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/otp/**",
                                "/api/auth/token/**",
                                "/api/auth/logout",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/ws/**",
                                "/ws-native/**"
                        ).permitAll()
//                        .requestMatchers("/ws/**",
//                                "/ws-native/**",
//                                "/topic/**",
//                                "/queue/**",
//                                "/app/**").authenticated()
                        .requestMatchers("/api/auth/sessions/**").authenticated()
                        .anyRequest().authenticated()

                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                new HttpStatusEntryPoint(HttpStatus.FORBIDDEN))
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

        // في البيئة التنموية، يمكنك استخدام "*" ولكن في الإنتاج حدد النطاقات المسموحة
        config.setAllowedOriginPatterns(List.of("*")); // استخدام patterns بدلاً من origins

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-CSRF-TOKEN", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization", "X-CSRF-TOKEN"));
        config.setAllowCredentials(true); // مهم للجلسات والكوكيز
        config.setMaxAge(3600L); // تخزين نتائج preflight لمدة ساعة

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
