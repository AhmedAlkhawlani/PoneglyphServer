package com.nova.poneglyph.service;

import com.nova.poneglyph.dto.ConversationDTO;
import com.nova.poneglyph.dto.MessageDTO;
import com.nova.poneglyph.dto.StartConversationRequest;
import com.nova.poneglyph.enums.MessageStatus;
import com.nova.poneglyph.model.Conversation;
import com.nova.poneglyph.model.User;
import com.nova.poneglyph.repository.ConversationRepository;
import com.nova.poneglyph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public List<ConversationDTO> getUserConversations(String phoneNumber, int page, int size) {
        Page<Conversation> conversations = conversationRepository
                .findByParticipantPhonesContaining(phoneNumber, PageRequest.of(page, size));

        return conversations.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

//    public ConversationDTO startConversation(String participant1, String participant2) {
//        // التحقق من وجود المستخدمين
//        User user1 = userRepository.findByPhoneNumber(participant1)
//                .orElseThrow(() -> new RuntimeException("User not found: " + participant1));
//
//        User user2 = userRepository.findByPhoneNumber(participant2)
//                .orElseThrow(() -> new RuntimeException("User not found: " + participant2));
//
//        // التحقق من وجود محادثة سابقة
//        List<Conversation> existing = conversationRepository
//                .findConversationBetweenUsers(participant1, participant2);
//
//        if (!existing.isEmpty()) {
//            return convertToDTO(existing.get(0));
//        }
//
//        // إنشاء محادثة جديدة
//        Conversation conversation = Conversation.builder()
//                .isGroup(false)
//                .participantPhones(List.of(participant1, participant2))
//                .build();
//
//        conversation = conversationRepository.save(conversation);
//        return convertToDTO(conversation);
//    }
    public ConversationDTO startConversation(StartConversationRequest request) {
    // التحقق من وجود المستخدمين
    User user1 = userRepository.findByPhoneNumber(request.getParticipant1Phone())
            .orElseThrow(() -> new RuntimeException("User not found: " + request.getParticipant1Phone()));

    User user2 = userRepository.findByPhoneNumber(request.getParticipant2Phone())
            .orElseThrow(() -> new RuntimeException("User not found: " + request.getParticipant2Phone()));

    // التحقق من وجود محادثة سابقة
    List<Conversation> existing = conversationRepository
            .findConversationBetweenUsers(request.getParticipant1Phone(), request.getParticipant2Phone());

    if (!existing.isEmpty()) {
        return convertToDTO(existing.get(0));
    }

    // إنشاء محادثة جديدة
    Conversation conversation = Conversation.builder()
            .isGroup(false)
            .participantIds(List.of(request.getParticipant1Phone(), request.getParticipant2Phone()))
            .build();

    conversation = conversationRepository.save(conversation);
    return convertToDTO(conversation);
}

    public List<MessageDTO> getConversationMessages(String conversationId, int page, int size) {
        return messageService.getMessagesByConversation(conversationId, page, size);
    }

//    private ConversationDTO convertToDTO(Conversation conversation) {
//        MessageDTO lastMessage = conversation.getMessages().isEmpty() ? null :
//                messageService.convertToDTO(conversation.getMessages().get(conversation.getMessages().size() - 1));
//
//        return ConversationDTO.builder()
//                .id(conversation.getId())
//                .createdAt(conversation.getCreatedAt())
//                .updatedAt(conversation.getUpdatedAt())
//                .participantIds(conversation.getParticipantIds())
//                .lastMessage(lastMessage)
//                .unreadCount(0) // يمكن حسابها لاحقاً
//                .build();
//    }

private ConversationDTO convertToDTO(Conversation conversation) {
    MessageDTO lastMessage = conversation.getMessages().isEmpty() ? null :
            messageService.convertToDTO(
                    conversation.getMessages().get(conversation.getMessages().size() - 1)
            );

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserPhone =auth.getName();
    // احسب عدد الرسائل غير المقروءة
    int unreadCount = (int) conversation.getMessages().stream()
            .filter(msg -> !msg.getSenderPhone().equals(currentUserPhone)) // ليست من المرسل الحالي
            .filter(msg -> msg.getStatus() != MessageStatus.SEEN)          // لم تُشاهد بعد
            .count();

    return ConversationDTO.builder()
            .id(conversation.getId())
            .createdAt(conversation.getCreatedAt())
            .updatedAt(conversation.getUpdatedAt())
            .participantIds(conversation.getParticipantIds())
            .lastMessage(lastMessage)
            .unreadCount(unreadCount)
            .build();
}

}
