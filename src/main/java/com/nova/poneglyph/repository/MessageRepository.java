package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.conversation.Conversation;
import com.nova.poneglyph.domain.message.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findTop50ByConversation_IdOrderByCreatedAtDescSequenceNumberDesc(UUID conversationId);

    List<Message> findTop1ByConversationOrderByCreatedAtDesc(Conversation conversation);

    List<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation);

    List<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);

    Optional<Message> findByLocalId(String localId);

    List<Message> findDistinctByConversationOrderByCreatedAtAsc(Conversation conversation, Pageable pageable);
}
