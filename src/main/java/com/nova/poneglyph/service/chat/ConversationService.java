package com.nova.poneglyph.service.chat;



import com.nova.poneglyph.domain.conversation.Conversation;
import com.nova.poneglyph.domain.conversation.Participant;
import com.nova.poneglyph.domain.enums.ConversationType;
import com.nova.poneglyph.domain.enums.ParticipantRole;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.chatDto.ConversationCreateDto;
import com.nova.poneglyph.dto.chatDto.ConversationDto;
import com.nova.poneglyph.dto.chatDto.MessageDto;
import com.nova.poneglyph.dto.chatDto.MessageSendDto;
import com.nova.poneglyph.exception.*;
import com.nova.poneglyph.repository.*;
import com.nova.poneglyph.service.UserGuardService;
import com.nova.poneglyph.service.audit.AuditService;
import com.nova.poneglyph.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class ConversationService {
//
//    private final ConversationRepository conversationRepository;
//    private final ParticipantRepository participantRepository;
//    private final UserRepository userRepository;
//    private final EncryptionUtil encryptionUtil;
//    private final UserGuardService userGuardService;
//    private final AuditService auditService;
//
//    @Transactional
//    public ConversationDto createConversation(ConversationCreateDto dto, UUID creatorId) {
//        User creator = userRepository.findById(creatorId)
//                .orElseThrow(() -> new ConversationException("User not found"));
//
//        // Check if user is allowed to create conversation
//        if (!userGuardService.canCreateConversation(creatorId)) {
//            throw new PermissionDeniedException("User cannot create conversations");
//        }
//
//        Set<User> participants = new HashSet<>(userRepository.findAllById(dto.getParticipantIds()));
//        participants.add(creator);
//
//        // Validate participants
//        if (participants.size() < 2) {
//            throw new ConversationException("At least 2 participants required");
//        }
//
//        // Check for existing direct conversation
//        if (dto.getType() == ConversationType.DIRECT && participants.size() == 2) {
//            Optional<Conversation> existing = findDirectConversation(
//                    participants.stream().map(User::getId).collect(Collectors.toSet())
//            );
//            if (existing.isPresent()) {
//                return convertToDto(existing.get());
//            }
//        }
//
//        // Create conversation
//        Conversation conversation = Conversation.builder()
//                .type(dto.getType())
//                .encrypted(true)
//                .encryptionKey(encryptionUtil.generateKey())
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
//            addParticipantToConversation(conversation, participant, role);
//        }
//
//        auditService.logConversationEvent(
//                creatorId,
//                "CONVERSATION_CREATE",
//                conversation.getConversationId().toString()
//        );
//
//        return convertToDto(conversation);
//    }
//
//    @Transactional
//    public void addParticipants(UUID conversationId, Set<UUID> userIds, UUID requesterId) {
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new ConversationException("Conversation not found"));
//
//        // Verify requester is admin/owner
//        Participant requester = participantRepository.findByConversationIdAndUserId(conversationId, requesterId)
//                .orElseThrow(() -> new ConversationException("User not in conversation"));
//
//        if (!requester.getRole().isAdmin()) {
//            throw new PermissionDeniedException("Insufficient privileges");
//        }
//
//        Set<User> newParticipants = new HashSet<>(userRepository.findAllById(userIds));
//
//        for (User participant : newParticipants) {
//            if (participantRepository.existsByConversationAndUser(conversation, participant)) {
//                continue;
//            }
//
//            addParticipantToConversation(conversation, participant, ParticipantRole.MEMBER);
//        }
//
//        auditService.logConversationEvent(
//                requesterId,
//                "CONVERSATION_ADD_MEMBER",
//                conversationId + ":" + userIds
//        );
//    }
//
//    @Transactional
//    public void removeParticipant(UUID conversationId, UUID userId, UUID requesterId) {
//        Participant participant = participantRepository.findByConversationIdAndUserId(conversationId, userId)
//                .orElseThrow(() -> new ConversationException("Participant not found"));
//
//        // Verify requester has permission
//        if (!requesterId.equals(participant.getUser().getId())) {
//            Participant requester = participantRepository.findByConversationIdAndUserId(conversationId, requesterId)
//                    .orElseThrow(() -> new PermissionDeniedException("Not in conversation"));
//
//            if (!requester.getRole().isAdmin()) {
//                throw new PermissionDeniedException("Insufficient privileges");
//            }
//        }
//
//        participantRepository.delete(participant);
//
//        // Delete conversation if last participant leaves
//        if (participantRepository.countByConversation(participant.getConversation()) == 0) {
//            conversationRepository.delete(participant.getConversation());
//        }
//
//        auditService.logConversationEvent(
//                requesterId,
//                "CONVERSATION_REMOVE_MEMBER",
//                conversationId + ":" + userId
//        );
//    }
//
//    @Transactional
//    public MessageDto sendMessage(MessageSendDto dto, UUID senderId) {
//        // Verify sender is participant
//        Participant senderParticipant = participantRepository.findByConversationIdAndUserId(
//                dto.getConversationId(), senderId
//        ).orElseThrow(() -> new ConversationException("User not in conversation"));
//
//        // Check if user is muted
//        if (senderParticipant.getMuteUntil() != null &&
//                senderParticipant.getMuteUntil().isAfter(OffsetDateTime.now())) {
//            throw new PermissionDeniedException("User is muted in this conversation");
//        }
//
//        // Create and send message
//        MessageDto message = messageService.sendMessage(
//                dto.getConversationId(),
//                senderId,
//                dto
//        );
//
//        // Update unread counts for other participants
//        updateUnreadCounts(dto.getConversationId(), senderId);
//
//        return message;
//    }
//
//    private void addParticipantToConversation(Conversation conversation, User user, ParticipantRole role) {
//        Participant participant = Participant.builder()
//                .conversation(conversation)
//                .user(user)
//                .role(role)
//                .joinedAt(OffsetDateTime.now())
//                .unreadCount(0)
//                .build();
//
//        participantRepository.save(participant);
//    }
//
//    private Optional<Conversation> findDirectConversation(Set<UUID> participantIds) {
//        if (participantIds.size() != 2) return Optional.empty();
//
//        List<UUID> ids = new ArrayList<>(participantIds);
//        return conversationRepository.findDirectConversation(ids.get(0), ids.get(1));
//    }
//
//    private ConversationDto convertToDto(Conversation conversation) {
//        return new ConversationDto(
//                conversation.getConversationId(),
//                conversation.getType(),
//                conversation.getEncryptionKey(),
//                conversation.getLastMessageAt()
//        );
//    }
//
//    private void updateUnreadCounts(UUID conversationId, UUID senderId) {
//        List<Participant> participants = participantRepository.findByConversationId(conversationId);
//        participants.stream()
//                .filter(p -> !p.getUser().getId().equals(senderId))
//                .forEach(p -> {
//                    p.setUnreadCount(p.getUnreadCount() + 1);
//                    participantRepository.save(p);
//                });
//    }
//}

@RequiredArgsConstructor
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final UserGuardService userGuardService;
    private final AuditService auditService;
    private final MessageService messageService; // مفقودة سابقًا

    @Transactional
    public ConversationDto createConversation(ConversationCreateDto dto, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ConversationException("User not found"));

        if (!userGuardService.canCreateConversation(creatorId)) {
            throw new PermissionDeniedException("User cannot create conversations");
        }

        Set<User> participants = new HashSet<>(userRepository.findAllById(dto.getParticipantIds()));
        participants.add(creator);

        if (participants.size() < 2) throw new ConversationException("At least 2 participants required");

        if (dto.getType() == ConversationType.DIRECT && participants.size() == 2) {
            List<UUID> ids = participants.stream().map(User::getId).sorted().toList();
            Optional<Conversation> existing = conversationRepository.findDirectConversation(ids.get(0), ids.get(1));
            if (existing.isPresent()) return convertToDto(existing.get());
        }

        Conversation conversation = Conversation.builder()
                .type(dto.getType())
                .encrypted(true)
                .encryptionKey(encryptionUtil.generateKey())
                .lastMessageAt(OffsetDateTime.now())
                .build();

        conversation = conversationRepository.save(conversation);

        for (User participant : participants) {
            ParticipantRole role = participant.getId().equals(creatorId) ? ParticipantRole.OWNER : ParticipantRole.MEMBER;
            addParticipantToConversation(conversation, participant, role);
        }

        auditService.logConversationEvent(creatorId, "CONVERSATION_CREATE", conversation.getId().toString());
        return convertToDto(conversation);
    }

    @Transactional
    public MessageDto sendMessage(MessageSendDto dto, UUID senderId) {
        Participant senderParticipant = participantRepository
                .findByConversation_IdAndUser_Id(dto.getConversationId(), senderId)
                .orElseThrow(() -> new ConversationException("User not in conversation"));

        if (senderParticipant.getMuteUntil() != null && senderParticipant.getMuteUntil().isAfter(OffsetDateTime.now())) {
            throw new PermissionDeniedException("User is muted in this conversation");
        }

        MessageDto message = messageService.sendMessage(dto.getConversationId(), senderId, dto);
        updateUnreadCounts(dto.getConversationId(), senderId);
        return message;
    }

    private ConversationDto convertToDto(Conversation c) {
        return new ConversationDto(c.getId(), c.getType(), c.getEncryptionKey(), c.getLastMessageAt());
    }

    private void updateUnreadCounts(UUID conversationId, UUID senderId) {
        List<Participant> participants = participantRepository.findByConversation_Id(conversationId);
        for (Participant p : participants) {
            if (!p.getUser().getId().equals(senderId)) {
                p.setUnreadCount((p.getUnreadCount() == null ? 0 : p.getUnreadCount()) + 1);
            }
        }
        participantRepository.saveAll(participants);
    }


        private void addParticipantToConversation(Conversation conversation, User user, ParticipantRole role) {
        Participant participant = Participant.builder()
                .conversation(conversation)
                .user(user)
                .role(role)
                .joinedAt(OffsetDateTime.now())
                .unreadCount(0)
                .build();

        participantRepository.save(participant);
    }
}
