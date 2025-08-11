package com.nova.poneglyph.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class RefreshToken {
    @Id
    private String userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Date expiration;

    @Column(nullable = false)
    private boolean revoked;

    public RefreshToken(String userId) {
        this.userId = userId;
        this.revoked = false;
    }
}
