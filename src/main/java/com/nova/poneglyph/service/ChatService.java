//package com.nova.poneglyph.service;
//
//
//
//import com.nova.poneglyph.domain.conversation.Conversation;
//import com.nova.poneglyph.domain.conversation.Participant;
//import com.nova.poneglyph.domain.enums.ParticipantRole;
//import com.nova.poneglyph.domain.user.User;
//
//
//import com.nova.poneglyph.dto.chatDto.ConversationCreateDto;
//import com.nova.poneglyph.dto.chatDto.MessageSendDto;
//import com.nova.poneglyph.exception.ConversationException;
//import com.nova.poneglyph.repository.ConversationRepository;
//import com.nova.poneglyph.repository.ParticipantRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.service.chat.MessageService;
//import com.nova.poneglyph.util.EncryptionUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.Set;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class ChatService {
//
//    private final ConversationRepository conversationRepository;
//    private final ParticipantRepository participantRepository;
//    private final UserRepository userRepository;
//    private final MessageService messageService;
//
//    @Transactional
//    public Conversation createConversation(ConversationCreateDto dto, UUID creatorId) {
//        User creator = userRepository.findById(creatorId)
//                .orElseThrow(() -> new ConversationException("User not found"));
//
//        List<User> participants = userRepository.findAllById(dto.getParticipantIds());
//        participants.add(creator);
//
//        if (participants.size() < 2) {
//            throw new ConversationException("At least 2 participants required");
//        }
//
//        Conversation conversation = Conversation.builder()
//                .type(dto.getType())
//                .encrypted(true)
//                .encryptionKey(EncryptionUtil.generateKey())
//                .lastMessageAt(OffsetDateTime.now())
//                .build();
//
//        conversation = conversationRepository.save(conversation);
//
//        // Add participants
//        for (User participant : participants) {
//            ParticipantRole role = participant.getId().equals(creatorId) ?
//                    ParticipantRole.OWNER : ParticipantRole.MEMBER;
//
//            Participant p = Participant.builder()
//                    .conversation(conversation)
//                    .user(participant)
//                    .role(role)
//                    .joinedAt(OffsetDateTime.now())
//                    .unreadCount(0)
//                    .build();
//
//            participantRepository.save(p);
//        }
//
//        return conversation;
//    }
//
//    @Transactional
//    public void addParticipants(UUID conversationId, Set<UUID> userIds, UUID requesterId) {
//        // Verify requester is admin/owner
//        Participant requester = participantRepository.findByConversation_IdAndUser_Id(conversationId, requesterId)
//                .orElseThrow(() -> new ConversationException("User not in conversation"));
//
//        if (!requester.getRole().isAdmin()) {
//            throw new ConversationException("Insufficient privileges");
//        }
//
//        List<User> newParticipants = userRepository.findAllById(userIds);
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new ConversationException("Conversation not found"));
//
//        for (User participant : newParticipants) {
//            if (participantRepository.existsByConversationAndUser(conversation, participant)) {
//                continue;
//            }
//
//            Participant p = Participant.builder()
//                    .conversation(conversation)
//                    .user(participant)
//                    .role(ParticipantRole.MEMBER)
//                    .joinedAt(OffsetDateTime.now())
//                    .unreadCount(0)
//                    .build();
//
//            participantRepository.save(p);
//        }
//    }
//
//    @Transactional
//    public void sendMessage(UUID conversationId, UUID senderId, MessageSendDto dto) {
//        // Verify sender is participant
//        participantRepository.findByConversation_IdAndUser_Id(conversationId, senderId)
//                .orElseThrow(() -> new ConversationException("User not in conversation"));
//
//        messageService.sendMessage(conversationId, senderId, dto);
//    }
//}
