//package com.nova.poneglyph.service;
//
//import com.nova.poneglyph.domain.conversation.Conversation;
//import com.nova.poneglyph.domain.conversation.Participant;
//import com.nova.poneglyph.domain.enums.ConversationType;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.repository.ConversationRepository;
//import com.nova.poneglyph.repository.ParticipantRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class ConversationService {
//    private final ConversationRepository conversationRepo;
//    private final ParticipantRepository participantRepo;
//
//    @Transactional
//    public Conversation ensureDirectConversation(User a, User b) {
//        // naive: search participants pairs; for production, add a pairing table or deterministic key
//        List<Participant> aParts = participantRepo.findByUser_Id(a.getId());
//        for (Participant p : aParts) {
//            Conversation c = p.getConversation();
//            if (c.getType() == ConversationType.DIRECT) {
//                Optional<Participant> other = participantRepo.findByConversation_IdAndUser_Id(c.getId(), b.getId());
//                if (other.isPresent()) return c;
//            }
//        }
//        // create new
//        Conversation c = Conversation.builder()
//                .type(ConversationType.DIRECT)
//                .encrypted(true)
//                .lastMessageAt(OffsetDateTime.now())
//                .build();
//        c.setId(UUID.randomUUID());
//        conversationRepo.save(c);
//
//        Participant p1 = Participant.builder().conversation(c).user(a).joinedAt(OffsetDateTime.now()).build();
//        p1.setId(UUID.randomUUID());
//        Participant p2 = Participant.builder().conversation(c).user(b).joinedAt(OffsetDateTime.now()).build();
//        p2.setId(UUID.randomUUID());
//        participantRepo.saveAll(List.of(p1, p2));
//        return c;
//    }
//}
