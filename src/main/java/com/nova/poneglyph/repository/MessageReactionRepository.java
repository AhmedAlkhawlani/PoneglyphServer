package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.message.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, MessageReaction.PK> {
    Optional<MessageReaction> findByMessage_IdAndUser_Id(UUID messageId, UUID userId);

}
