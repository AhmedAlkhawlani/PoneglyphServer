package com.nova.poneglyph.service.chat;



import com.nova.poneglyph.domain.conversation.Conversation;
import com.nova.poneglyph.domain.conversation.Participant;
import com.nova.poneglyph.domain.enums.ConversationType;
import com.nova.poneglyph.domain.enums.MessageType;
import com.nova.poneglyph.domain.enums.ParticipantRole;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.conversation.*;
import com.nova.poneglyph.exception.*;
import com.nova.poneglyph.repository.*;
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

//@RequiredArgsConstructor
//@Service
//public class ConversationService {
//
//    private final ConversationRepository conversationRepository;
//    private final ParticipantRepository participantRepository;
//    private final UserRepository userRepository;
//    private final EncryptionUtil encryptionUtil;
//    private final UserGuardService userGuardService;
//    private final AuditService auditService;
//    private final MessageService messageService; // مفقودة سابقًا
//
//    @Transactional
//    public ConversationDto createConversation(ConversationCreateDto dto, UUID creatorId) {
//        User creator = userRepository.findById(creatorId)
//                .orElseThrow(() -> new ConversationException("User not found"));
//
//        if (!userGuardService.canCreateConversation(creatorId)) {
//            throw new PermissionDeniedException("User cannot create conversations");
//        }
//
//        Set<User> participants = new HashSet<>(userRepository.findAllById(dto.getParticipantIds()));
//        participants.add(creator);
//
//        if (participants.size() < 2) throw new ConversationException("At least 2 participants required");
//
//        if (dto.getType() == ConversationType.DIRECT && participants.size() == 2) {
//            List<UUID> ids = participants.stream().map(User::getId).sorted().toList();
//            Optional<Conversation> existing = conversationRepository.findDirectConversation(ids.get(0), ids.get(1));
//            if (existing.isPresent()) return convertToDto(existing.get());
//        }
//
//        Conversation conversation = Conversation.builder()
//                .type(dto.getType())
//                .encrypted(true)
//                .encryptionKey(encryptionUtil.generateKey())
//                .lastMessageAt(OffsetDateTime.now())
//                .build();
//
//        conversation = conversationRepository.save(conversation);
//
//        for (User participant : participants) {
//            ParticipantRole role = participant.getId().equals(creatorId) ? ParticipantRole.OWNER : ParticipantRole.MEMBER;
//            addParticipantToConversation(conversation, participant, role);
//        }
//
//        auditService.logConversationEvent(creatorId, "CONVERSATION_CREATE", conversation.getId().toString());
//        return convertToDto(conversation);
//    }
//
//    @Transactional
//    public MessageDto sendMessage(MessageSendDto dto, UUID senderId) {
//        Participant senderParticipant = participantRepository
//                .findByConversation_IdAndUser_Id(dto.getConversationId(), senderId)
//                .orElseThrow(() -> new ConversationException("User not in conversation"));
//
//        if (senderParticipant.getMuteUntil() != null && senderParticipant.getMuteUntil().isAfter(OffsetDateTime.now())) {
//            throw new PermissionDeniedException("User is muted in this conversation");
//        }
//
//        MessageDto message = messageService.sendMessage(dto.getConversationId(), senderId, dto);
//        updateUnreadCounts(dto.getConversationId(), senderId);
//        return message;
//    }
//
//    private ConversationDto convertToDto(Conversation c) {
//        return new ConversationDto(c.getId(), c.getType(), c.getEncryptionKey(), c.getLastMessageAt());
//    }
//
//    private void updateUnreadCounts(UUID conversationId, UUID senderId) {
//        List<Participant> participants = participantRepository.findByConversation_Id(conversationId);
//        for (Participant p : participants) {
//            if (!p.getUser().getId().equals(senderId)) {
//                p.setUnreadCount((p.getUnreadCount() == null ? 0 : p.getUnreadCount()) + 1);
//            }
//        }
//        participantRepository.saveAll(participants);
//    }
//
//
//        private void addParticipantToConversation(Conversation conversation, User user, ParticipantRole role) {
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
//}
// ConversationService.java

import com.nova.poneglyph.domain.message.Message;

import com.nova.poneglyph.repository.ConversationRepository;
import com.nova.poneglyph.repository.MessageRepository;
import com.nova.poneglyph.repository.ParticipantRepository;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.util.EncryptionUtil;
import com.nova.poneglyph.util.PhoneUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(UUID userId) {
        List<Participant> participants = participantRepository.findByUser_Id(userId);
        return participants.stream()
                .map(Participant::getConversation)
                .map(conversation -> convertToDto(conversation, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public ConversationDTO createConversation(UUID userId, CreateConversationRequest request) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // إنشاء المحادثة
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());
        conversation.setType(ConversationType.valueOf(request.getType()));
        conversation.setEncrypted(true);
        conversation.setLastMessageAt(OffsetDateTime.now());
        conversation.setEncryptionKey(EncryptionUtil.generateKey());
        conversationRepository.save(conversation);

        // إضافة المستخدم الحالي كمشارك
        addParticipant(conversation, currentUser, ParticipantRole.ADMIN);

        // إضافة المشاركين الآخرين
        for (String phone : request.getParticipantPhones()) {
            String normalizedPhone = PhoneUtil.normalizePhone(phone);
            User user = userRepository.findByNormalizedPhone(normalizedPhone)
                    .orElseThrow(() -> new RuntimeException("User not found with phone: " + phone));
            addParticipant(conversation, user, ParticipantRole.MEMBER);
        }

        // إذا كانت هناك رسالة أولية، إرسالها
        if (request.getInitialMessage() != null && !request.getInitialMessage().isEmpty()) {
            SendMessageRequest messageRequest = new SendMessageRequest();
            messageRequest.setMessageType("TEXT");
            messageRequest.setContent(request.getInitialMessage());
            sendMessage(userId, conversation.getId(), messageRequest);
        }

        return convertToDto(conversation, userId);
    }

    private void addParticipant(Conversation conversation, User user, ParticipantRole role) {
        Participant participant = new Participant();
        participant.setId(UUID.randomUUID());
        participant.setConversation(conversation);
        participant.setUser(user);
        participant.setRole(role);
        participant.setJoinedAt(OffsetDateTime.now());
        participant.setUnreadCount(0);
        participantRepository.save(participant);
    }

    @Transactional
    public MessageDTO sendMessage(UUID userId, UUID conversationId, SendMessageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Participant participant = participantRepository.findByConversationAndUser(conversation, user)
                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));

        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setConversation(conversation);
        message.setSender(user);
        message.setMessageType(MessageType.valueOf(request.getMessageType()));

        // 🔐 التشفير باستخدام مفتاح المحادثة
        byte[] encryptedContent = encryptionUtil.encryptToBytes(request.getContent(), conversation.getEncryptionKey());
        message.setEncryptedContent(encryptedContent);

        message.setContentHash(encryptionUtil.hash(request.getContent()));
        message.setCreatedAt(OffsetDateTime.now());

        messageRepository.save(message);

        conversation.setLastMessageAt(OffsetDateTime.now());
        conversationRepository.save(conversation);

        List<Participant> otherParticipants = participantRepository.findByConversation(conversation).stream()
                .filter(p -> !p.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        for (Participant p : otherParticipants) {
            p.setUnreadCount(p.getUnreadCount() + 1);
            participantRepository.save(p);
        }

        return convertToDto(message);
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(UUID userId, UUID conversationId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // التحقق من أن المستخدم مشارك في المحادثة
        participantRepository.findByConversationAndUser_Id(conversation, userId)
                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));

        Pageable pageable = PageRequest.of(page, size);
        List<Message> messages = messageRepository.findByConversationOrderByCreatedAtDesc(conversation, pageable);

        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID userId, UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Participant participant = participantRepository.findByConversationAndUser_Id(conversation, userId)
                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));

        participant.setUnreadCount(0);
        participantRepository.save(participant);
    }

    @Transactional
    public void deleteConversation(UUID userId, UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Participant participant = participantRepository.findByConversationAndUser_Id(conversation, userId)
                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));

        // إذا كان المستخدم هو ADMIN، يمكنه حذف المحادثة كليًا
        if (participant.getRole() == ParticipantRole.ADMIN) {
            conversationRepository.delete(conversation);
        } else {
            // وإلا، يغادر المحادثة فقط
            participant.setLeftAt(OffsetDateTime.now());
            participantRepository.save(participant);
        }
    }

    private ConversationDTO convertToDto(Conversation conversation, UUID currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setType(conversation.getType().name());
        dto.setEncrypted(conversation.isEncrypted());
        dto.setLastMessageAt(conversation.getLastMessageAt());

        // جلب المشاركين
        List<Participant> participants = participantRepository.findByConversation(conversation);
        dto.setParticipants(participants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));

        // جلب آخر رسالة
        List<Message> lastMessages = messageRepository.findTop1ByConversationOrderByCreatedAtDesc(conversation);
        if (!lastMessages.isEmpty()) {
            dto.setLastMessage(convertToDto(lastMessages.get(0)));
        }

        // جلب عدد الرسائل غير المقروءة للمستخدم الحالي
        Participant currentParticipant = participants.stream()
                .filter(p -> p.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        if (currentParticipant != null) {
            dto.setUnreadCount(currentParticipant.getUnreadCount());
        }

        return dto;
    }

    private ParticipantDTO convertToDto(Participant participant) {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setUserId(participant.getUser().getId());
        dto.setPhoneNumber(participant.getUser().getPhoneNumber());
        dto.setDisplayName(participant.getUser().getDisplayName());
        dto.setRole(participant.getRole().name());
        dto.setJoinedAt(participant.getJoinedAt());
        dto.setLeftAt(participant.getLeftAt());
        dto.setUnreadCount(participant.getUnreadCount());
        return dto;
    }

    private MessageDTO convertToDto(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderPhone(message.getSender().getPhoneNumber());
        dto.setSenderName(message.getSender().getDisplayName());
        dto.setMessageType(message.getMessageType().name());

        // 🔓 فك التشفير باستخدام مفتاح المحادثة
        String decryptedContent = encryptionUtil.decryptFromBytes(
                message.getEncryptedContent(),
                message.getConversation().getEncryptionKey()
        );
        dto.setContent(decryptedContent);

        dto.setCreatedAt(message.getCreatedAt());
        dto.setSequenceNumber(message.getSequenceNumber());

        if (message.getMediaAttachments() != null && !message.getMediaAttachments().isEmpty()) {
            dto.setMediaAttachments(message.getMediaAttachments().stream()
                    .map(media -> {
                        MediaDTO mediaDto = new MediaDTO();
                        mediaDto.setId(media.getId());
                        mediaDto.setFileUrl(media.getFileUrl());
                        mediaDto.setFileType(media.getFileType());
                        mediaDto.setFileSize(media.getFileSize());
                        mediaDto.setThumbnailUrl(media.getThumbnailUrl());
                        mediaDto.setDurationSec(media.getDurationSec());
                        return mediaDto;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
