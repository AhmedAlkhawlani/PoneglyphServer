package com.nova.poneglyph.config.v2;

// In a @Configuration class
import com.nova.poneglyph.config.v2.kms.KmsProvider;
import com.nova.poneglyph.config.v2.kms.NoopKmsProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class SecurityBeansConfig {
    @Bean
    public KmsProvider kmsProvider() {
        // TODO: replace with real KMS provider implementation in prod
        return new NoopKmsProvider();
    }
}
