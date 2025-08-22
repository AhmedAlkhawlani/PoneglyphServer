package com.nova.poneglyph.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "backup")
public class BackupConfig {
    private String directory;
    private String dataDirectory;
    private boolean encrypt;
    private String encryptionKey;
    private int retentionDays = 30;
}
