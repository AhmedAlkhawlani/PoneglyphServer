package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.message.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, MessageStatus.PK> {
    Optional<MessageStatus> findByMessage_IdAndUser_Id(UUID messageId, UUID userId);

}
