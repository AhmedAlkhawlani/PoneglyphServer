package com.nova.poneglyph.repo;

import com.nova.poneglyph.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByUserId(String userId);
    Optional<UserToken> findByToken(String token);
}

