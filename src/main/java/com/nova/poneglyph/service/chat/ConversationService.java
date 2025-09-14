
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
    private final MessageStatusService messageStatusService;

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
//        if (request.getInitialMessage() != null && !request.getInitialMessage().isEmpty()) {
//            SendMessageRequest msgReq = new SendMessageRequest();
//            msgReq.setMessageType("TEXT");
//            msgReq.setContent(request.getInitialMessage());
//            // نستخدم sendMessage الداخلي
//            sendMessage(userId, conv.getId(), msgReq);
//        }
        // في createConversation، استبدل sendMessage الداخلي بـ messageService.sendMessage
        if (request.getInitialMessage() != null && !request.getInitialMessage().isEmpty()) {
            SendMessageRequest msgReq = new SendMessageRequest();
            msgReq.setMessageType("TEXT");
            msgReq.setContent(request.getInitialMessage());
            // استخدم messageService
            messageService.sendMessage(userId, conv.getId(), msgReq);
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
//        messageStatusService.markRead(userId, conversationId);
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
