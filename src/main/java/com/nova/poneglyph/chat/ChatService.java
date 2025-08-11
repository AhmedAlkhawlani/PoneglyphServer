//package com.nova.poneglyph.chat;
//
//import com.nova.poneglyph.exception.ApiException;
//import com.nova.poneglyph.exception.ErrorCode;
//import com.nova.poneglyph.user.User;
//import com.nova.poneglyph.user.UserRepository;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class ChatService {
//
//    private final ChatRepository chatRepository;
//    private final UserRepository userRepository;
//    private final ChatMapper mapper;
//
//    @Transactional(readOnly = true)
//    public List<ChatResponse> getChatsByReceiverId(Authentication currentUser) {
//        String username = currentUser.getName();
//        // جلب المستخدم من قاعدة البيانات باستخدام الاسم
//        User user = userRepository.findByName(username)
//                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND, "User not found"));
//
//        final String userId = user.getId(); // نستخدم المعرف الصحيح هنا
//
////        final String userId = currentUser.getName();
//        return chatRepository.findChatsBySenderId(userId)
//                .stream()
//                .map(c -> mapper.toChatResponse(c, userId))
//                .toList();
//    }
//
//    public String createChat(String senderId, String receiverId) {
//
//        Optional<Chat> existingChat = chatRepository.findChatByReceiverAndSender(senderId, receiverId);
//        if (existingChat.isPresent()) {
//            return existingChat.get().getId();
//        }
//
//        User sender = userRepository.findByPublicId(senderId)
//                .orElseThrow(() ->  new EntityNotFoundException("User with id " + senderId + " not found"));
//        User receiver = userRepository.findByPublicId(receiverId)
//                .orElseThrow(() ->  new EntityNotFoundException("User with id " + receiverId + " not found"));
//
//        Chat chat = new Chat();
//        chat.setSender(sender);
//        chat.setRecipient(receiver);
//
//        Chat savedChat = chatRepository.save(chat);
//        return savedChat.getId();
//    }
//}
