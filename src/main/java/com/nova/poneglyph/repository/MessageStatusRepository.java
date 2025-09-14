package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.enums.DeliveryStatus;
import com.nova.poneglyph.domain.message.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, MessageStatus.PK> {
    Optional<MessageStatus> findByMessage_IdAndUser_Id(UUID messageId, UUID userId);

//    Optional<MessageStatus> findByMessage_IdAndUser_Id(UUID messageId, UUID userId);

    @Modifying
    @Transactional
    @Query("update MessageStatus ms set ms.status = :status, ms.updatedAt = :now "
            + "where ms.message.id = :messageId and ms.user.id = :userId and ms.status <> :status")
    int updateStatusForUser(@Param("messageId") UUID messageId,
                            @Param("userId") UUID userId,
                            @Param("status") DeliveryStatus status,
                            @Param("now") OffsetDateTime now);

    @Modifying
    @Transactional
    @Query("update MessageStatus ms set ms.status = :status, ms.updatedAt = :now "
            + "where ms.message.conversation.id = :conversationId and ms.user.id = :userId and ms.status <> :status")
    int markAllInConversationForUser(@Param("conversationId") UUID conversationId,
                                     @Param("userId") UUID userId,
                                     @Param("status") DeliveryStatus status,
                                     @Param("now") OffsetDateTime now);
}
