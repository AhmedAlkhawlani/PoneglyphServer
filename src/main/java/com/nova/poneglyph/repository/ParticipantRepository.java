package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.conversation.Conversation;
import com.nova.poneglyph.domain.conversation.Participant;
import com.nova.poneglyph.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
    List<Participant> findByUser_Id(UUID userId);

    Optional<Participant> findByConversationAndUser_Id(Conversation conversationId, UUID userId);

//    Optional<Participant> findByConversation_IdAndUser_UserId(UUID conversationId, UUID userId);
    boolean existsByConversationAndUser(Conversation conversation, User user);
    List<Participant> findByConversation_Id(UUID conversationId);
    List<Participant> findByConversation(Conversation conversation);
    long countByConversation(Conversation conversation);

    Optional<Participant> findByConversationAndUser(Conversation conversation, User user);

    Optional<Participant> findByConversation_IdAndUser_Id(UUID conversationId, UUID userId);
}
