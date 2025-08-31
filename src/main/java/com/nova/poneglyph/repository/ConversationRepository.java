package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    // محادثة مباشرة بين مستخدمين (اتفق على ترتيب ثابت لتفادي التكرار)
    @Query("""
      select c from Conversation c
      join Participant p1 on p1.conversation = c and p1.user.id = :u1
      join Participant p2 on p2.conversation = c and p2.user.id = :u2
      where c.type = com.nova.poneglyph.domain.enums.ConversationType.DIRECT
    """)
    Optional<Conversation> findDirectConversation(UUID u1, UUID u2);
    @Query("SELECT COUNT(p) FROM Participant p WHERE p.conversation.id = :convId")
    long countByConversationId(@Param("convId") UUID convId);
}
