package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.moderation.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, UserBlock.PK> {
    boolean existsByBlocker_IdAndBlocked_Id(UUID blockerId, UUID blockedId);
}
