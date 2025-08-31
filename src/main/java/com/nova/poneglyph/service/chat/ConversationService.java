//package com.nova.poneglyph.service.chat;
//
//
//
//import com.nova.poneglyph.domain.conversation.Conversation;
//import com.nova.poneglyph.domain.conversation.Participant;
//import com.nova.poneglyph.domain.enums.ConversationType;
//import com.nova.poneglyph.domain.enums.MessageType;
//import com.nova.poneglyph.domain.enums.ParticipantRole;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.dto.conversation.*;
//import com.nova.poneglyph.exception.*;
//import com.nova.poneglyph.repository.*;
//import com.nova.poneglyph.util.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.time.OffsetDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
////
////@Service
////@RequiredArgsConstructor
////public class ConversationService {
////
////    private final ConversationRepository conversationRepository;
////    private final ParticipantRepository participantRepository;
////    private final UserRepository userRepository;
////    private final EncryptionUtil encryptionUtil;
////    private final UserGuardService userGuardService;
////    private final AuditService auditService;
////
////    @Transactional
////    public ConversationDto createConversation(ConversationCreateDto dto, UUID creatorId) {
////        User creator = userRepository.findById(creatorId)
////                .orElseThrow(() -> new ConversationException("User not found"));
////
////        // Check if user is allowed to create conversation
////        if (!userGuardService.canCreateConversation(creatorId)) {
////            throw new PermissionDeniedException("User cannot create conversations");
////        }
////
////        Set<User> participants = new HashSet<>(userRepository.findAllById(dto.getParticipantIds()));
////        participants.add(creator);
////
////        // Validate participants
////        if (participants.size() < 2) {
////            throw new ConversationException("At least 2 participants required");
////        }
////
////        // Check for existing direct conversation
////        if (dto.getType() == ConversationType.DIRECT && participants.size() == 2) {
////            Optional<Conversation> existing = findDirectConversation(
////                    participants.stream().map(User::getId).collect(Collectors.toSet())
////            );
////            if (existing.isPresent()) {
////                return convertToDto(existing.get());
////            }
////        }
////
////        // Create conversation
////        Conversation conversation = Conversation.builder()
////                .type(dto.getType())
////                .encrypted(true)
////                .encryptionKey(encryptionUtil.generateKey())
////                .lastMessageAt(OffsetDateTime.now())
////                .build();
////
////        conversation = conversationRepository.save(conversation);
////
////        // Add participants
////        for (User participant : participants) {
////            ParticipantRole role = participant.getId().equals(creatorId) ?
////                    ParticipantRole.OWNER : ParticipantRole.MEMBER;
////
////            addParticipantToConversation(conversation, participant, role);
////        }
////
////        auditService.logConversationEvent(
////                creatorId,
////                "CONVERSATION_CREATE",
////                conversation.getConversationId().toString()
////        );
////
////        return convertToDto(conversation);
////    }
////
////    @Transactional
////    public void addParticipants(UUID conversationId, Set<UUID> userIds, UUID requesterId) {
////        Conversation conversation = conversationRepository.findById(conversationId)
////                .orElseThrow(() -> new ConversationException("Conversation not found"));
////
////        // Verify requester is admin/owner
////        Participant requester = participantRepository.findByConversationIdAndUserId(conversationId, requesterId)
////                .orElseThrow(() -> new ConversationException("User not in conversation"));
////
////        if (!requester.getRole().isAdmin()) {
////            throw new PermissionDeniedException("Insufficient privileges");
////        }
////
////        Set<User> newParticipants = new HashSet<>(userRepository.findAllById(userIds));
////
////        for (User participant : newParticipants) {
////            if (participantRepository.existsByConversationAndUser(conversation, participant)) {
////                continue;
////            }
////
////            addParticipantToConversation(conversation, participant, ParticipantRole.MEMBER);
////        }
////
////        auditService.logConversationEvent(
////                requesterId,
////                "CONVERSATION_ADD_MEMBER",
////                conversationId + ":" + userIds
////        );
////    }
////
////    @Transactional
////    public void removeParticipant(UUID conversationId, UUID userId, UUID requesterId) {
////        Participant participant = participantRepository.findByConversationIdAndUserId(conversationId, userId)
////                .orElseThrow(() -> new ConversationException("Participant not found"));
////
////        // Verify requester has permission
////        if (!requesterId.equals(participant.getUser().getId())) {
////            Participant requester = participantRepository.findByConversationIdAndUserId(conversationId, requesterId)
////                    .orElseThrow(() -> new PermissionDeniedException("Not in conversation"));
////
////            if (!requester.getRole().isAdmin()) {
////                throw new PermissionDeniedException("Insufficient privileges");
////            }
////        }
////
////        participantRepository.delete(participant);
////
////        // Delete conversation if last participant leaves
////        if (participantRepository.countByConversation(participant.getConversation()) == 0) {
////            conversationRepository.delete(participant.getConversation());
////        }
////
////        auditService.logConversationEvent(
////                requesterId,
////                "CONVERSATION_REMOVE_MEMBER",
////                conversationId + ":" + userId
////        );
////    }
////
////    @Transactional
////    public MessageDto sendMessage(MessageSendDto dto, UUID senderId) {
////        // Verify sender is participant
////        Participant senderParticipant = participantRepository.findByConversationIdAndUserId(
////                dto.getConversationId(), senderId
////        ).orElseThrow(() -> new ConversationException("User not in conversation"));
////
////        // Check if user is muted
////        if (senderParticipant.getMuteUntil() != null &&
////                senderParticipant.getMuteUntil().isAfter(OffsetDateTime.now())) {
////            throw new PermissionDeniedException("User is muted in this conversation");
////        }
////
////        // Create and send message
////        MessageDto message = messageService.sendMessage(
////                dto.getConversationId(),
////                senderId,
////                dto
////        );
////
////        // Update unread counts for other participants
////        updateUnreadCounts(dto.getConversationId(), senderId);
////
////        return message;
////    }
////
////    private void addParticipantToConversation(Conversation conversation, User user, ParticipantRole role) {
////        Participant participant = Participant.builder()
////                .conversation(conversation)
////                .user(user)
////                .role(role)
////                .joinedAt(OffsetDateTime.now())
////                .unreadCount(0)
////                .build();
////
////        participantRepository.save(participant);
////    }
////
////    private Optional<Conversation> findDirectConversation(Set<UUID> participantIds) {
////        if (participantIds.size() != 2) return Optional.empty();
////
////        List<UUID> ids = new ArrayList<>(participantIds);
////        return conversationRepository.findDirectConversation(ids.get(0), ids.get(1));
////    }
////
////    private ConversationDto convertToDto(Conversation conversation) {
////        return new ConversationDto(
////                conversation.getConversationId(),
////                conversation.getType(),
////                conversation.getEncryptionKey(),
////                conversation.getLastMessageAt()
////        );
////    }
////
////    private void updateUnreadCounts(UUID conversationId, UUID senderId) {
////        List<Participant> participants = participantRepository.findByConversationId(conversationId);
////        participants.stream()
////                .filter(p -> !p.getUser().getId().equals(senderId))
////                .forEach(p -> {
////                    p.setUnreadCount(p.getUnreadCount() + 1);
////                    participantRepository.save(p);
////                });
////    }
////}
//
////@RequiredArgsConstructor
////@Service
////public class ConversationService {
////
////    private final ConversationRepository conversationRepository;
////    private final ParticipantRepository participantRepository;
////    private final UserRepository userRepository;
////    private final EncryptionUtil encryptionUtil;
////    private final UserGuardService userGuardService;
////    private final AuditService auditService;
////    private final MessageService messageService; // مفقودة سابقًا
////
////    @Transactional
////    public ConversationDto createConversation(ConversationCreateDto dto, UUID creatorId) {
////        User creator = userRepository.findById(creatorId)
////                .orElseThrow(() -> new ConversationException("User not found"));
////
////        if (!userGuardService.canCreateConversation(creatorId)) {
////            throw new PermissionDeniedException("User cannot create conversations");
////        }
////
////        Set<User> participants = new HashSet<>(userRepository.findAllById(dto.getParticipantIds()));
////        participants.add(creator);
////
////        if (participants.size() < 2) throw new ConversationException("At least 2 participants required");
////
////        if (dto.getType() == ConversationType.DIRECT && participants.size() == 2) {
////            List<UUID> ids = participants.stream().map(User::getId).sorted().toList();
////            Optional<Conversation> existing = conversationRepository.findDirectConversation(ids.get(0), ids.get(1));
////            if (existing.isPresent()) return convertToDto(existing.get());
////        }
////
////        Conversation conversation = Conversation.builder()
////                .type(dto.getType())
////                .encrypted(true)
////                .encryptionKey(encryptionUtil.generateKey())
////                .lastMessageAt(OffsetDateTime.now())
////                .build();
////
////        conversation = conversationRepository.save(conversation);
////
////        for (User participant : participants) {
////            ParticipantRole role = participant.getId().equals(creatorId) ? ParticipantRole.OWNER : ParticipantRole.MEMBER;
////            addParticipantToConversation(conversation, participant, role);
////        }
////
////        auditService.logConversationEvent(creatorId, "CONVERSATION_CREATE", conversation.getId().toString());
////        return convertToDto(conversation);
////    }
////
////    @Transactional
////    public MessageDto sendMessage(MessageSendDto dto, UUID senderId) {
////        Participant senderParticipant = participantRepository
////                .findByConversation_IdAndUser_Id(dto.getConversationId(), senderId)
////                .orElseThrow(() -> new ConversationException("User not in conversation"));
////
////        if (senderParticipant.getMuteUntil() != null && senderParticipant.getMuteUntil().isAfter(OffsetDateTime.now())) {
////            throw new PermissionDeniedException("User is muted in this conversation");
////        }
////
////        MessageDto message = messageService.sendMessage(dto.getConversationId(), senderId, dto);
////        updateUnreadCounts(dto.getConversationId(), senderId);
////        return message;
////    }
////
////    private ConversationDto convertToDto(Conversation c) {
////        return new ConversationDto(c.getId(), c.getType(), c.getEncryptionKey(), c.getLastMessageAt());
////    }
////
////    private void updateUnreadCounts(UUID conversationId, UUID senderId) {
////        List<Participant> participants = participantRepository.findByConversation_Id(conversationId);
////        for (Participant p : participants) {
////            if (!p.getUser().getId().equals(senderId)) {
////                p.setUnreadCount((p.getUnreadCount() == null ? 0 : p.getUnreadCount()) + 1);
////            }
////        }
////        participantRepository.saveAll(participants);
////    }
////
////
////        private void addParticipantToConversation(Conversation conversation, User user, ParticipantRole role) {
////        Participant participant = Participant.builder()
////                .conversation(conversation)
////                .user(user)
////                .role(role)
////                .joinedAt(OffsetDateTime.now())
////                .unreadCount(0)
////                .build();
////
////        participantRepository.save(participant);
////    }
////}
//// ConversationService.java
//
//import com.nova.poneglyph.domain.message.Message;
//
//import com.nova.poneglyph.repository.ConversationRepository;
//import com.nova.poneglyph.repository.MessageRepository;
//import com.nova.poneglyph.repository.ParticipantRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.util.EncryptionUtil;
//import com.nova.poneglyph.util.PhoneUtil;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class ConversationService {
//
//    private final ConversationRepository conversationRepository;
//    private final ParticipantRepository participantRepository;
//    private final MessageRepository messageRepository;
//    private final UserRepository userRepository;
//    private final EncryptionUtil encryptionUtil;
//
//    @Transactional(readOnly = true)
//    public List<ConversationDTO> getUserConversations(UUID userId) {
//        // الحصول على كافة المشاركات الخاصة بالمستخدم وجلب المحادثات المرتبطة
//        List<Participant> participants = participantRepository.findByUser_Id(userId);
//        return participants.stream()
//                .map(Participant::getConversation)
//                .distinct()
//                .map(conv -> convertToDto(conv, userId))
//                .collect(Collectors.toList());
//    }
//
//    private ConversationDTO convertToDto(Conversation conversation, UUID currentUserId) {
//        ConversationDTO dto = new ConversationDTO();
//        dto.setId(conversation.getId());
//        dto.setType(conversation.getType().name());
//        dto.setEncrypted(conversation.isEncrypted());
//        dto.setLastMessageAt(conversation.getLastMessageAt());
//
//        List<Participant> participants = participantRepository.findByConversation(conversation);
//        dto.setParticipants(participants.stream().map(this::convertToDto).collect(Collectors.toList()));
//
//        // ======= حساب displayName اعتماداً على المشاركين (بدون حقل title في الـ Entity) =======
//        String displayName = null;
//        if (conversation.getType() == ConversationType.DIRECT) {
//            for (Participant p : participants) {
//                if (!p.getUser().getId().equals(currentUserId)) {
//                    User other = p.getUser();
//                    displayName = (other.getDisplayName() != null && !other.getDisplayName().isEmpty())
//                            ? other.getDisplayName()
//                            : other.getPhoneNumber();
//                    break;
//                }
//            }
//            // احتياط: لو لم نجد غير currentUser
//            if (displayName == null && !participants.isEmpty()) {
//                User fallback = participants.get(0).getUser();
//                displayName = fallback.getDisplayName() != null ? fallback.getDisplayName() : fallback.getPhoneNumber();
//            }
//        } else {
//            // للمحادثات الجماعية: اختر اسم المحادثة من مكان آخر أو اجمع أسماء
//            // هنا نفترض أنك لا تملك حقل title في Entity، فنبني اسمًا من أسماء المشاركين (باستثناء currentUser إن أردت)
//            displayName = participants.stream()
//                    .filter(p -> !p.getUser().getId().equals(currentUserId))
//                    .map(p -> {
//                        User u = p.getUser();
//                        return (u.getDisplayName() != null && !u.getDisplayName().isEmpty()) ? u.getDisplayName() : u.getPhoneNumber();
//                    })
//                    .limit(3) // اختصار الطول
//                    .collect(Collectors.joining(", "));
//            if (displayName.isEmpty()) {
//                // fallback لجميع الأسماء
//                displayName = participants.stream()
//                        .map(p -> {
//                            User u = p.getUser();
//                            return (u.getDisplayName() != null && !u.getDisplayName().isEmpty()) ? u.getDisplayName() : u.getPhoneNumber();
//                        })
//                        .limit(3)
//                        .collect(Collectors.joining(", "));
//            }
//        }
//        dto.setDisplayName(displayName);
//        // ======= نهاية تعيين displayName =======
//
//        // باقي الحقول: lastMessage, unreadCount
//        List<Message> lastMessages = messageRepository.findTop1ByConversationOrderByCreatedAtDesc(conversation);
//        if (!lastMessages.isEmpty()) dto.setLastMessage(convertToDto(lastMessages.get(0)));
//
//        Participant currentParticipant = participants.stream()
//                .filter(p -> p.getUser().getId().equals(currentUserId))
//                .findFirst()
//                .orElse(null);
//
//        dto.setUnreadCount(currentParticipant != null ? (currentParticipant.getUnreadCount() == null ? 0 : currentParticipant.getUnreadCount()) : 0);
//
//        return dto;
//    }
//
////    @Transactional(readOnly = true)
////    public List<ConversationDTO> getUserConversations(UUID userId) {
////        List<Participant> participants = participantRepository.findByUser_Id(userId);
////        return participants.stream()
////                .map(Participant::getConversation)
////                .map(conversation -> convertToDto(conversation, userId))
////                .collect(Collectors.toList());
////    }
//
//    @Transactional
//    public ConversationDTO createConversation(UUID userId, CreateConversationRequest request) {
//        User currentUser = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // للمحادثات الشخصية، تحقق أولاً إذا كانت هناك محادثة موجودة بالفعل
//        if ("DIRECT".equals(request.getType()) && request.getParticipantPhones().size() == 1) {
//            String participantPhone = request.getParticipantPhones().get(0);
//            String defaultRegion = PhoneUtil.extractRegionFromE164(participantPhone);
//            String normalizedPhone = PhoneUtil.normalizeForStorage(participantPhone, defaultRegion);
//
//            // البحث عن محادثة شخصية موجودة بالفعل
//            Optional<Conversation> existingConversation = findExistingDirectConversation(userId, normalizedPhone);
//            if (existingConversation.isPresent()) {
//                return convertToDto(existingConversation.get(), userId);
//            }
//        }
//
//        // إنشاء المحادثة الجديدة
//        Conversation conversation = new Conversation();
//        conversation.setId(UUID.randomUUID());
//        conversation.setType(ConversationType.valueOf(request.getType()));
//        conversation.setEncrypted(true);
//        conversation.setEncryptionKey(EncryptionUtil.generateKey());
//        conversation.setLastMessageAt(OffsetDateTime.now());
//        conversationRepository.save(conversation);
//
//        // إضافة المستخدم الحالي كمشارك
//        addParticipant(conversation, currentUser, ParticipantRole.ADMIN);
//
//        // إضافة المشاركين الآخرين
//        for (String phone : request.getParticipantPhones()) {
//            String defaultRegion = PhoneUtil.extractRegionFromE164(phone);
//            String normalizedPhone = PhoneUtil.normalizeForStorage(phone, defaultRegion);
//
//            User user = userRepository.findByNormalizedPhone(normalizedPhone)
//                    .orElseThrow(() -> new RuntimeException("User not found with phone: " + phone));
//            addParticipant(conversation, user, ParticipantRole.MEMBER);
//        }
//
//        // إذا كانت هناك رسالة أولية، إرسالها
//        if (request.getInitialMessage() != null && !request.getInitialMessage().isEmpty()) {
//            SendMessageRequest messageRequest = new SendMessageRequest();
//            messageRequest.setMessageType("TEXT");
//            messageRequest.setContent(request.getInitialMessage());
//            sendMessage(userId, conversation.getId(), messageRequest);
//        }
//
//        return convertToDto(conversation, userId);
//    }
//
//    private Optional<Conversation> findExistingDirectConversation(UUID userId, String participantNormalizedPhone) {
//        // الحصول على جميع محادثات المستخدم
//        List<Participant> userParticipants = participantRepository.findByUser_Id(userId);
//
//        for (Participant userParticipant : userParticipants) {
//            Conversation conversation = userParticipant.getConversation();
//
//            // التحقق من أن المحادثة شخصية
//            if (conversation.getType() == ConversationType.DIRECT) {
//                // الحصول على جميع المشاركين في المحادثة
//                List<Participant> allParticipants = participantRepository.findByConversation(conversation);
//
//                // يجب أن يكون هناك مشاركان فقط في المحادثة الشخصية
//                if (allParticipants.size() == 2) {
//                    // البحث عن المشارك الآخر
//                    for (Participant participant : allParticipants) {
//                        if (!participant.getUser().getId().equals(userId)) {
//                            // التحقق من أن رقم الهاتف يتطابق
//                            if (participant.getUser().getNormalizedPhone().equals(participantNormalizedPhone)) {
//                                return Optional.of(conversation);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return Optional.empty();
//    }
//
//private void addParticipant(Conversation conversation, User user, ParticipantRole role) {
//    // للمحادثات الشخصية، جعل كلا الطرفين أعضاء عاديين
//    if (conversation.getType() == ConversationType.DIRECT) {
//        role = ParticipantRole.MEMBER;
//    }
//
//    Participant participant = new Participant();
//    participant.setId(UUID.randomUUID());
//    participant.setConversation(conversation);
//    participant.setUser(user);
//    participant.setRole(role);
//    participant.setJoinedAt(OffsetDateTime.now());
//    participant.setUnreadCount(0);
//    participantRepository.save(participant);
//}
//
//    @Transactional
//    public MessageDTO sendMessage(UUID userId, UUID conversationId, SendMessageRequest request) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        Participant participant = participantRepository.findByConversationAndUser(conversation, user)
//                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));
//
//        // التحقق من وجود رسالة بنفس localId مسبقاً
//        if (request.getLocalId() != null) {
//            Optional<Message> existingMessage = messageRepository.findByLocalId(request.getLocalId());
//            if (existingMessage.isPresent()) {
//                // إذا كانت الرسالة موجودة مسبقاً، إعادة الرسالة الموجودة
//                return convertToDto(existingMessage.get());
//            }
//        }
//
//        Message message = new Message();
//        message.setId(UUID.randomUUID());
//        message.setConversation(conversation);
//        message.setSender(user);
//        message.setMessageType(MessageType.valueOf(request.getMessageType()));
//
//        // حفظ localId من الطلب
//        message.setLocalId(request.getLocalId());
//
//        // 🔐 التشفير باستخدام مفتاح المحادثة
//        byte[] encryptedContent = encryptionUtil.encryptToBytes(request.getContent(), conversation.getEncryptionKey());
//        message.setEncryptedContent(encryptedContent);
//
//        message.setContentHash(encryptionUtil.hash(request.getContent()));
//        message.setCreatedAt(OffsetDateTime.now());
//
//        messageRepository.save(message);
//
//        // تحديث lastMessageAt للمحادثة
//        conversation.setLastMessageAt(OffsetDateTime.now());
//        conversationRepository.save(conversation);
//
//        // زيادة عداد الرسائل غير المقروءة للمشاركين الآخرين
//        List<Participant> otherParticipants = participantRepository.findByConversation(conversation).stream()
//                .filter(p -> !p.getUser().getId().equals(userId))
//                .collect(Collectors.toList());
//
//        for (Participant p : otherParticipants) {
//            p.setUnreadCount(p.getUnreadCount() + 1);
//            participantRepository.save(p);
//        }
//
//        return convertToDto(message);
//    }
//    @Transactional(readOnly = true)
//    public List<MessageDTO> getMessages(UUID userId, UUID conversationId, int page, int size) {
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        // التحقق من أن المستخدم مشارك في المحادثة
//        participantRepository.findByConversationAndUser_Id(conversation, userId)
//                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));
//
//        Pageable pageable = PageRequest.of(page, size);
//        // استخدام DISTINCT لتجنب الرسائل المكررة
//        List<Message> messages = messageRepository.findDistinctByConversationOrderByCreatedAtAsc(conversation, pageable);
//
//        return messages.stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public void markAsRead(UUID userId, UUID conversationId) {
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        Participant participant = participantRepository.findByConversationAndUser_Id(conversation, userId)
//                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));
//
//        participant.setUnreadCount(0);
//        participantRepository.save(participant);
//    }
//
//    @Transactional
//    public void deleteConversation(UUID userId, UUID conversationId) {
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        Participant participant = participantRepository.findByConversationAndUser_Id(conversation, userId)
//                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));
//
//        // إذا كان المستخدم هو ADMIN، يمكنه حذف المحادثة كليًا
//        if (participant.getRole() == ParticipantRole.ADMIN) {
//            conversationRepository.delete(conversation);
//        } else {
//            // وإلا، يغادر المحادثة فقط
//            participant.setLeftAt(OffsetDateTime.now());
//            participantRepository.save(participant);
//        }
//    }
//
////    private ConversationDTO convertToDto(Conversation conversation, UUID currentUserId) {
////        ConversationDTO dto = new ConversationDTO();
////        dto.setId(conversation.getId());
////        dto.setType(conversation.getType().name());
////        dto.setEncrypted(conversation.isEncrypted());
////        dto.setLastMessageAt(conversation.getLastMessageAt());
////
////        // جلب المشاركين
////        List<Participant> participants = participantRepository.findByConversation(conversation);
////        dto.setParticipants(participants.stream()
////                .map(this::convertToDto)
////                .collect(Collectors.toList()));
////
////        // جلب آخر رسالة
////        List<Message> lastMessages = messageRepository.findTop1ByConversationOrderByCreatedAtDesc(conversation);
////        if (!lastMessages.isEmpty()) {
////            dto.setLastMessage(convertToDto(lastMessages.get(0)));
////        }
////
////        // جلب عدد الرسائل غير المقروءة للمستخدم الحالي
////        Participant currentParticipant = participants.stream()
////                .filter(p -> p.getUser().getId().equals(currentUserId))
////                .findFirst()
////                .orElse(null);
////
////        if (currentParticipant != null) {
////            dto.setUnreadCount(currentParticipant.getUnreadCount());
////        }
////
////        return dto;
////    }
//
//    private ParticipantDTO convertToDto(Participant participant) {
//        ParticipantDTO dto = new ParticipantDTO();
//        dto.setUserId(participant.getUser().getId());
//        dto.setPhoneNumber(participant.getUser().getPhoneNumber());
//        dto.setDisplayName(participant.getUser().getDisplayName());
//        dto.setRole(participant.getRole().name());
//        dto.setJoinedAt(participant.getJoinedAt());
//        dto.setLeftAt(participant.getLeftAt());
//        dto.setUnreadCount(participant.getUnreadCount());
//        return dto;
//    }
//
//    private MessageDTO convertToDto(Message message) {
//        MessageDTO dto = new MessageDTO();
//        dto.setId(message.getId());
//        dto.setConversationId(message.getConversation().getId());
//        dto.setSenderId(message.getSender().getId());
//        dto.setSenderPhone(message.getSender().getPhoneNumber());
//        dto.setSenderName(message.getSender().getDisplayName());
//        dto.setMessageType(message.getMessageType().name());
//        // إعادة localId إذا كان محفوظاً
//        if (message.getLocalId() != null) {
//            dto.setLocalId(message.getLocalId());
//        }
//        // 🔓 فك التشفير باستخدام مفتاح المحادثة
//        String decryptedContent = encryptionUtil.decryptFromBytes(
//                message.getEncryptedContent(),
//                message.getConversation().getEncryptionKey()
//        );
//        dto.setContent(decryptedContent);
//
//        dto.setCreatedAt(message.getCreatedAt());
//        dto.setSequenceNumber(message.getSequenceNumber());
//
//        if (message.getMediaAttachments() != null && !message.getMediaAttachments().isEmpty()) {
//            dto.setMediaAttachments(message.getMediaAttachments().stream()
//                    .map(media -> {
//                        MediaDTO mediaDto = new MediaDTO();
//                        mediaDto.setId(media.getId());
//                        mediaDto.setFileUrl(media.getFileUrl());
//                        mediaDto.setFileType(media.getFileType());
//                        mediaDto.setFileSize(media.getFileSize());
//                        mediaDto.setThumbnailUrl(media.getThumbnailUrl());
//                        mediaDto.setDurationSec(media.getDurationSec());
//                        return mediaDto;
//                    })
//                    .collect(Collectors.toList()));
//        }
//
//        return dto;
//    }
//
//    @Transactional(readOnly = true)
//    public ConversationDTO getConversation(UUID userId, UUID conversationId) {
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        // التحقق من أن المستخدم مشارك في المحادثة
//        participantRepository.findByConversationAndUser_Id(conversation, userId)
//                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));
//
//        return convertToDto(conversation, userId);
//    }
//}

package com.nova.poneglyph.service.chat;

import com.nova.poneglyph.domain.conversation.Conversation;
import com.nova.poneglyph.domain.conversation.Participant;
import com.nova.poneglyph.domain.enums.ConversationType;
import com.nova.poneglyph.domain.enums.MessageType;
import com.nova.poneglyph.domain.enums.ParticipantRole;
import com.nova.poneglyph.domain.message.Message;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.conversation.*;
import com.nova.poneglyph.exception.ConversationException;
import com.nova.poneglyph.exception.PermissionDeniedException;
import com.nova.poneglyph.repository.ConversationRepository;
import com.nova.poneglyph.repository.MessageRepository;
import com.nova.poneglyph.repository.ParticipantRepository;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.service.WebSocketService;
import com.nova.poneglyph.util.EncryptionUtil;
import com.nova.poneglyph.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ConversationService — خدمة إدارة المحادثات (DIRECT & GROUP)
 * - تحسب displayName لكل مستدعي (currentUserId)
 * - تدير المشاركين، الرسائل، والعدادات (unread)
 */
@Service
@RequiredArgsConstructor
public class ConversationService {
    // داخل تعريف الكلاس كـ final field (بما أن @RequiredArgsConstructor مستخدم)
    private final WebSocketService socketService;
    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final MessageService messageService; // افترض وجوده لإعادة استخدام منطق الرسائل

    // ===========================
    // قائمة المحادثات للمستخدم
    // ===========================
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(UUID userId) {
        // جلب المشاركات فقط للمستخدم ثم تحويل المحادثات إلى DTO محسوب لها displayName
        List<Participant> userParticipants = participantRepository.findByUser_Id(userId);
        // تجنب التكرار باستخدام distinct() على المحادثة
        return userParticipants.stream()
                .map(Participant::getConversation)
                .filter(Objects::nonNull)
                .distinct()
                .map(conv -> convertToDto(conv, userId))
                .collect(Collectors.toList());
    }

    // ===========================
    // تحويل Conversation -> DTO (مع displayName و participantCount)
    // ===========================
    private ConversationDTO convertToDto(Conversation conversation, UUID currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setType(conversation.getType() != null ? conversation.getType().name() : ConversationType.DIRECT.name());
        dto.setEncrypted(conversation.isEncrypted());
        dto.setLastMessageAt(conversation.getLastMessageAt());

        // جلب المشاركين (توقع أن participantRepository يجلب User داخل Participant)
        List<Participant> participants = participantRepository.findByConversation(conversation);

        // participant DTOs
        List<ParticipantDTO> participantDTOs = participants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        dto.setParticipants(participantDTOs);

        // participantCount: استخدم دالة repo إذا كانت متوفرة (أسرع)، وإلا استخدم طول القائمة
        Integer participantCount;
        try {
            // حاول استخدام countByConversationId إذا وُجد في repository
            // افتراض وجود method countByConversationId(UUID convId)
            long count = participantRepository.countByConversation(conversation);
            participantCount = (int) count;
        } catch (Throwable t) {
            // fallback
            participantCount = participants.size();
        }
        dto.setParticipantCount(participantCount);

        // ===== حساب displayName اعتمادا على نوع المحادثة =====
        String displayName = null;
        if (conversation.getType() == ConversationType.DIRECT) {
            // ابحث عن المشارك الذي ليس currentUserId
            for (Participant p : participants) {
                User u = p.getUser();
                if (u != null && !u.getId().equals(currentUserId)) {
                    displayName = (u.getDisplayName() != null && !u.getDisplayName().isEmpty()) ? u.getDisplayName() : u.getPhoneNumber();
                    break;
                }
            }
            // احتياطي: لو كان current user هو الوحيد المسجل، استخدم أول مشارك
            if (displayName == null && !participants.isEmpty()) {
                User fallback = participants.get(0).getUser();
                displayName = (fallback != null && fallback.getDisplayName() != null && !fallback.getDisplayName().isEmpty())
                        ? fallback.getDisplayName() : (fallback != null ? fallback.getPhoneNumber() : "");
            }
        } else {
            // GROUP: استخدم title المخزن إذا وُجد (Conversation قد يحتوي على title) وإلا اصنع اسم من الأعضاء
            if (conversation.getTitle() != null && !conversation.getTitle().isEmpty()) {
                displayName = conversation.getTitle();
            } else {
                // جمع أسماء (استثناء currentUser اختياري — هنا نستبعد currentUser لعرض اسم الآخرين)
                displayName = participants.stream()
                        .filter(p -> {
                            User u = p.getUser();
                            return u != null && !u.getId().equals(currentUserId);
                        })
                        .map(p -> {
                            User u = p.getUser();
                            return (u.getDisplayName() != null && !u.getDisplayName().isEmpty()) ? u.getDisplayName() : u.getPhoneNumber();
                        })
                        .limit(3)
                        .collect(Collectors.joining(", "));
                if (displayName == null || displayName.isEmpty()) {
                    // fallback لعرض أي أسماء
                    displayName = participants.stream()
                            .map(p -> {
                                User u = p.getUser();
                                return (u.getDisplayName() != null && !u.getDisplayName().isEmpty()) ? u.getDisplayName() : u.getPhoneNumber();
                            })
                            .limit(3)
                            .collect(Collectors.joining(", "));
                }
            }
        }
        dto.setDisplayName(displayName != null ? displayName : "");

        // آخر رسالة (preview/full) — نحاول جلب أحدث رسالة
        List<Message> lastMessages = messageRepository.findTop1ByConversationOrderByCreatedAtDesc(conversation);
        if (lastMessages != null && !lastMessages.isEmpty()) {
            dto.setLastMessage(convertToDto(lastMessages.get(0)));
        }

        // unread count للمستخدم الحالي
        Participant currentParticipant = participants.stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        dto.setUnreadCount(currentParticipant != null ? (currentParticipant.getUnreadCount() == null ? 0 : currentParticipant.getUnreadCount()) : 0);

        return dto;
    }

    // ===========================
    // تحويل Participant -> DTO
    // ===========================
    private ParticipantDTO convertToDto(Participant participant) {
        ParticipantDTO dto = new ParticipantDTO();
        if (participant == null) return dto;
        User u = participant.getUser();
        dto.setUserId(u != null ? u.getId() : null);
        dto.setPhoneNumber(u != null ? u.getPhoneNumber() : null);
        dto.setDisplayName(u != null ? u.getDisplayName() : null);
        dto.setRole(participant.getRole() != null ? participant.getRole().name() : ParticipantRole.MEMBER.name());
        dto.setJoinedAt(participant.getJoinedAt());
        dto.setLeftAt(participant.getLeftAt());
        dto.setUnreadCount(participant.getUnreadCount());
        return dto;
    }

    // ===========================
    // تحويل Message -> DTO (مع فك التشفير)
    // ===========================
    private MessageDTO convertToDto(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation() != null ? message.getConversation().getId() : null);
        dto.setSenderId(message.getSender() != null ? message.getSender().getId() : null);
        dto.setSenderPhone(message.getSender() != null ? message.getSender().getPhoneNumber() : null);
        dto.setSenderName(message.getSender() != null ? message.getSender().getDisplayName() : null);
        dto.setMessageType(message.getMessageType() != null ? message.getMessageType().name() : null);
        if (message.getLocalId() != null) dto.setLocalId(message.getLocalId());

        // فك التشفير؛ احمِ ضد الأخطاء
        try {
            String decrypted = encryptionUtil.decryptFromBytes(message.getEncryptedContent(), message.getConversation().getEncryptionKey());
            dto.setContent(decrypted);
        } catch (Throwable t) {
            dto.setContent(null);
        }

        dto.setCreatedAt(message.getCreatedAt());
        dto.setSequenceNumber(message.getSequenceNumber());

        // media attachments, statuses, reactions... إذا أردت أضيف هنا تحويلهم
        return dto;
    }

    // ===========================
    // إنشاء محادثة (DIRECT أو GROUP) — يدعم title للمجموعات
    // ===========================
    @Transactional
    public ConversationDTO createConversation(UUID userId, CreateConversationRequest request) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // جمع المشاركين من أرقام الهاتف (normalize)
        Set<User> participants = new HashSet<>();
        participants.add(currentUser);

        if (request.getParticipantPhones() != null) {
            for (String phone : request.getParticipantPhones()) {
                String defaultRegion = PhoneUtil.extractRegionFromE164(phone);
                String normalizedPhone = PhoneUtil.normalizeForStorage(phone, defaultRegion);
                User user = userRepository.findByNormalizedPhone(normalizedPhone)
                        .orElseThrow(() -> new RuntimeException("User not found with phone: " + phone));
                participants.add(user);
            }
        }

        if (participants.size() < 2) throw new ConversationException("At least 2 participants required");

        // لــ DIRECT: محاولة العثور على محادثة حالية بين الطرفين
        if ("DIRECT".equalsIgnoreCase(request.getType()) && participants.size() == 2) {
            List<UUID> ids = participants.stream().map(User::getId).sorted().collect(Collectors.toList());
            Optional<Conversation> existing = conversationRepository.findDirectConversation(ids.get(0), ids.get(1));
            if (existing.isPresent()) return convertToDto(existing.get(), userId);
        }

        // إنشاء المحادثة
        Conversation conv = new Conversation();
        conv.setId(UUID.randomUUID());
        conv.setType(ConversationType.valueOf(request.getType()));
        conv.setEncrypted(true);
        conv.setEncryptionKey(encryptionUtil.generateKey());
        conv.setLastMessageAt(OffsetDateTime.now());

        // إذا هناك title (للمجموعات) خزّنه في الكيان (يفترض أنك أضفت الحقل)
        if (request.getTitle() != null && !request.getTitle().isEmpty() && conv.getType() != ConversationType.DIRECT) {
            conv.setTitle(request.getTitle());
        }

        conversationRepository.save(conv);

        // إضافة المشاركين
        for (User u : participants) {
            ParticipantRole role = u.getId().equals(userId) ? ParticipantRole.ADMIN : ParticipantRole.MEMBER;
            addParticipant(conv, u, role);
        }

        // إرسال رسالة أولية إن وُجدت
        if (request.getInitialMessage() != null && !request.getInitialMessage().isEmpty()) {
            SendMessageRequest msgReq = new SendMessageRequest();
            msgReq.setMessageType("TEXT");
            msgReq.setContent(request.getInitialMessage());
            // نستخدم sendMessage الداخلي
            sendMessage(userId, conv.getId(), msgReq);
        }

        return convertToDto(conv, userId);
    }

    // ===========================
    // البحث عن محادثة DIRECT قائمة بحسب رقم مُطَبَّع
    // ===========================
    private Optional<Conversation> findExistingDirectConversation(UUID userId, String participantNormalizedPhone) {
        // احصل على مشاركات المستخدم
        List<Participant> userParticipants = participantRepository.findByUser_Id(userId);

        for (Participant up : userParticipants) {
            Conversation conv = up.getConversation();
            if (conv == null || conv.getType() != ConversationType.DIRECT) continue;

            List<Participant> allParts = participantRepository.findByConversation(conv);
            if (allParts.size() != 2) continue;

            for (Participant p : allParts) {
                User u = p.getUser();
                if (u == null) continue;
                if (u.getId().equals(userId)) continue;
                if (participantNormalizedPhone.equals(u.getNormalizedPhone())) {
                    return Optional.of(conv);
                }
            }
        }
        return Optional.empty();
    }

    // ===========================
    // إضافة مشترك (مستقل عن نوع المحادثة)
    // ===========================
    private void addParticipant(Conversation conversation, User user, ParticipantRole role) {
        if (conversation.getType() == ConversationType.DIRECT) {
            role = ParticipantRole.MEMBER; // في المحادثات المباشرة كلاهما MEMBER
        }

        Participant p = new Participant();
        p.setId(UUID.randomUUID());
        p.setConversation(conversation);
        p.setUser(user);
        p.setRole(role);
        p.setJoinedAt(OffsetDateTime.now());
        p.setUnreadCount(0);
        participantRepository.save(p);

        // (اختياري) تحديث participants_count في الجدول conversations لو كنت تخزن هذا الحقل
        try {
            int cnt = (int) participantRepository.countByConversation(conversation);
            conversation.setParticipantsCount(cnt);
            conversationRepository.save(conversation);
        } catch (Throwable ignored) { /* ليس إجباريًا */ }
    }

    // ===========================
    // إرسال رسالة
    // ===========================
    @Transactional
    public MessageDTO sendMessage(UUID userId, UUID conversationId, SendMessageRequest request) {
        User sender = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found"));

        Participant participant = participantRepository.findByConversationAndUser(conversation, sender)
                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));

        if (participant.getMuteUntil() != null && participant.getMuteUntil().isAfter(OffsetDateTime.now())) {
            throw new PermissionDeniedException("User is muted in this conversation");
        }

        // تفادي الرسائل المكررة حسب localId
        if (request.getLocalId() != null) {
            Optional<Message> existing = messageRepository.findByLocalId(request.getLocalId());
            if (existing.isPresent()) {
                return convertToDto(existing.get());
            }
        }

        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setConversation(conversation);
        message.setSender(sender);
        message.setMessageType(MessageType.valueOf(request.getMessageType()));
        message.setLocalId(request.getLocalId());

        byte[] encrypted = encryptionUtil.encryptToBytes(request.getContent(), conversation.getEncryptionKey());
        message.setEncryptedContent(encrypted);
        message.setContentHash(encryptionUtil.hash(request.getContent()));
        message.setCreatedAt(OffsetDateTime.now());

        messageRepository.save(message);

        // تحديث lastMessageAt في المحادثة
        conversation.setLastMessageAt(OffsetDateTime.now());
        conversationRepository.save(conversation);

        // زيادة unread للمشاركين الآخرين
        List<Participant> others = participantRepository.findByConversation(conversation).stream()
                .filter(p -> p.getUser() != null && !p.getUser().getId().equals(userId))
                .collect(Collectors.toList());
        for (Participant p : others) {
            p.setUnreadCount((p.getUnreadCount() == null ? 0 : p.getUnreadCount()) + 1);
            participantRepository.save(p);
        }

        // إرجاع DTO للرسالة
        return convertToDto(message);
    }

    // ===========================
    // جلب رسائل (pagination)
    // ===========================
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(UUID userId, UUID conversationId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // التحقق من مشاركة المستخدم
        participantRepository.findByConversationAndUser_Id(conversation, userId)
                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));

        Pageable pageable = PageRequest.of(page, size);
        List<Message> messages = messageRepository.findDistinctByConversationOrderByCreatedAtAsc(conversation, pageable);

        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // ===========================
    // تمييز كمقروء
    // ===========================
    @Transactional
    public void markAsRead(UUID userId, UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Participant p = participantRepository.findByConversationAndUser_Id(conversation, userId)
                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));

        p.setUnreadCount(0);
        participantRepository.save(p);
    }

    // ===========================
    // إزالة/مغادرة المحادثة أو حذفها (بحسب الدور)
    // ===========================
    @Transactional
    public void deleteOrLeaveConversation(UUID userId, UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Participant participant = participantRepository.findByConversationAndUser_Id(conversation, userId)
                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));

        if (participant.getRole() == ParticipantRole.ADMIN) {
            // الحذف الكامل
            conversationRepository.delete(conversation);
        } else {
            // مغادرة المستخدم فقط
            participant.setLeftAt(OffsetDateTime.now());
            participantRepository.save(participant);
        }
    }

    // ===========================
    // إضافة مشاركين (API)
    // ===========================
    @Transactional
    public void addParticipants(UUID conversationId, Set<UUID> userIds, UUID requesterId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Participant requester = participantRepository.findByConversationAndUser_Id(conversation, requesterId)
                .orElseThrow(() -> new RuntimeException("User not in conversation"));

        if (!requester.getRole().isAdmin()) throw new PermissionDeniedException("Insufficient privileges");

        List<User> users = userRepository.findAllById(userIds);
        for (User u : users) {
            boolean exists = participantRepository.existsByConversationAndUser(conversation, u);
            if (!exists) {
                addParticipant(conversation, u, ParticipantRole.MEMBER);
            }
        }
    }

    // ===========================
    // إزالة مشارك
    // ===========================
    @Transactional
    public void removeParticipant(UUID conversationId, UUID userIdToRemove, UUID requesterId) {
        Participant target = participantRepository.findByConversationAndUser_Id(
                conversationRepository.findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found")),
                userIdToRemove
        ).orElseThrow(() -> new RuntimeException("Participant not found"));

        // تحقق صلاحيات
        if (!requesterId.equals(target.getUser().getId())) {
            Participant req = participantRepository.findByConversationAndUser_Id(target.getConversation(), requesterId)
                    .orElseThrow(() -> new RuntimeException("Requester not in conversation"));
            if (!req.getRole().isAdmin()) throw new PermissionDeniedException("Insufficient privileges");
        }

        participantRepository.delete(target);

        // إذا بقي صفر مشاركين احذف المحادثة
        long remaining = participantRepository.countByConversation(target.getConversation());
        if (remaining == 0) {
            conversationRepository.delete(target.getConversation());
        } else {
            // تحديث participants_count إن كنت تخزنها
            try {
                Conversation conv = target.getConversation();
                conv.setParticipantsCount((int) remaining);
                conversationRepository.save(conv);
            } catch (Throwable ignored) {}
        }
    }
    @Transactional
    public void updateConversationTitle(UUID requesterId, UUID conversationId, String newTitle) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // تأكد أن requester عضو في المحادثة
        Participant requester = participantRepository.findByConversationAndUser_Id(conv, requesterId)
                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));

        // فقط ADMIN/OWNER يمكنهم تغيير عنوان المجموعة (للمحافظاة على أمان الأعمال)
        if (!requester.getRole().isAdmin()) {
            throw new PermissionDeniedException("Insufficient privileges to change conversation title");
        }

        // ضبط العنوان وحفظ المحادثة
        conv.setTitle(newTitle);
        conversationRepository.save(conv);

        // بناء DTO مُحَدَّث وإشعار المشتركين
        ConversationDTO dto = convertToDto(conv, requesterId);
        try {
            socketService.notifyConversationUpdated(conversationId, dto);
        } catch (Throwable ignored) {
            // لا نفشل العملية الرئيسية بسبب فشل إشعار الويب سوكيت
        }
    }

        @Transactional(readOnly = true)
    public ConversationDTO getConversation(UUID userId, UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // التحقق من أن المستخدم مشارك في المحادثة
        participantRepository.findByConversationAndUser_Id(conversation, userId)
                .orElseThrow(() -> new RuntimeException("User is not a participant in this conversation"));

        return convertToDto(conversation, userId);
    }

}
