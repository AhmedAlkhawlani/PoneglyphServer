//package com.nova.poneglyph.message;
//
//import com.nova.poneglyph.chat.Chat;
//import com.nova.poneglyph.chat.ChatRepository;
//import com.nova.poneglyph.file.FileService;
//import com.nova.poneglyph.notification.Notification;
//import com.nova.poneglyph.notification.NotificationService;
//import com.nova.poneglyph.notification.NotificationType;
//import com.nova.poneglyph.user.User;
//import com.nova.poneglyph.user.UserRepository;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class MessageService {
//    private final MessageRepository messageRepository;
//    private final ChatRepository chatRepository;
//    private final MessageMapper mapper;
//    private final NotificationService notificationService;
//    private final FileService fileService;
//    private final UserRepository userRepository;
//
//    private final SimpMessagingTemplate messagingTemplate; // أضف هذا
//    public void updateMessageState(String messageId, MessageState newState) {
//        Message message = messageRepository.findById(messageId)
//                .orElseThrow(() -> new RuntimeException("Message not found"));
//        message.setState(newState);
//        messageRepository.save(message);
//        // إرسال التحديث عبر WebSocket
//        messagingTemplate.convertAndSend(
//                "/topic/message-state/" + messageId,
//                new MessageStateUpdate(messageId, newState)
//        );
//    }
//
//
//@Transactional
//public MessageResponse saveMessage(MessageRequest messageRequest) {
//    Chat chat = chatRepository.findById(messageRequest.getChatId())
//            .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
//
//    if (!chat.getSender().getId().equals(messageRequest.getSenderId()) &&
//            !chat.getRecipient().getId().equals(messageRequest.getSenderId())) {
//        throw new SecurityException("User is not a participant in this chat");
//    }
//
//    Message message = new Message();
//    message.setContent(messageRequest.getContent());
//    message.setChat(chat);
//    message.setSenderId(messageRequest.getSenderId());
//    message.setReceiverId(messageRequest.getReceiverId());
//    message.setType(messageRequest.getType());
//    message.setCreatedDate(LocalDateTime.now());
//    message.setState(MessageState.SENT);
//
//    messageRepository.save(message);
//
//    sendMessageNotification(chat, message);
//
//    // يمكنك استخدام mapper لتحويل Message إلى MessageResponse
////    return new MessageResponse(message.getId(), message.getContent(), message.getCreatedDate(), message.getType());
//    // تحويل إلى DTO
//    return MessageResponse.builder()
//            .id(message.getId())
//            .content(message.getContent())
//            .type(message.getType())
//            .state(message.getState())
//            .senderId(message.getSenderId())
//            .receiverId(message.getReceiverId())
//            .createdAt(message.getCreatedDate())
////            .media(message.getMedia())
//            .build();
//}
//
//    private void sendMessageNotification(Chat chat, Message message) {
//
//        User user = userRepository.findByPublicId(message.getReceiverId())
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String currentUser =auth.getName();
//
//        Notification notification = Notification.builder()
//                .chatId(chat.getId())
//                .fromName(currentUser)
//                .messageId(message.getId())
//                .messageType(message.getType())
//                .content(message.getContent())
//                .senderId(message.getSenderId())
//                .receiverId(message.getReceiverId())
//                .type(NotificationType.MESSAGE)
//                .chatName(chat.getTargetChatName(message.getSenderId()))
//                .timestamp(message.getCreatedDate().toString())
//                .build();
//
//        notificationService.sendToUser(user.getName(), notification);
////        notificationService.sendToChatTopic(chat.getId(), notification);
//    }
//
//    public List<MessageResponse> findChatMessages(String chatId) {
//        return messageRepository.findMessagesByChatId(chatId)
//                .stream()
//                .map(mapper::toMessageResponse)
//                .toList();
//    }
//
//
//
//@Transactional
//public void setMessagesToSeen(String chatId, String receiverId, Authentication authentication) {
//    Chat chat = chatRepository.findById(chatId)
//            .orElseThrow(() -> new RuntimeException("Chat not found"));
//
//    String currentUser = authentication.getName();
//
//    // جلب الرسائل غير المقروءة الموجهة للـ receiverId
//    List<Message> unseenMessages = messageRepository.findMessagesByChatIdAndReceiverIdAndStateNot(chatId, receiverId,MessageState.SEEN);
//
//    if (!unseenMessages.isEmpty()) {
//        // تحديث حالة الرسائل إلى SEEN
//        unseenMessages.forEach(message -> {
//            if (message.getState() != MessageState.SEEN) {
//                message.setState(MessageState.SEEN);
//
//                messageRepository.save(message);
//
//            }
//        });
//
////        messageRepository.saveAll(unseenMessages);
//
////        // احصل على المرسل (طرف المحادثة الآخر) من أول رسالة فقط
////        String senderId = unseenMessages.get(0).getSenderId();
////        User sender=userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("sender not found"));
////
////        // لا ترسل إشعار لنفس المستخدم الذي قام بالقراءة
////        if (!senderId.equals(receiverId)) {
////            Notification notification = Notification.builder()
////                    .chatId(chatId)
////                    .fromName(currentUser)
////                    .type(NotificationType.SEEN)
////                    .receiverId(senderId)     // المرسل الذي سيستقبل الإشعار
////                    .senderId(receiverId)     // من قام بالقراءة
////                    .content("تمت قراءة رسائلك من قبل ")
////                    .build();
////
////            notificationService.sendToUser(sender.getName(), notification);
////        }
//    }
//}
//
//private String getSenderId(Chat chat, Authentication authentication) {
//    if (chat.getSender().getId().equals(authentication.getName())) {
//        return chat.getSender().getId();
//    }
//    return chat.getRecipient().getId();
//}
//
//    private String getRecipientId(Chat chat, Authentication authentication) {
//        if (chat.getSender().getId().equals(authentication.getName())) {
//            return chat.getRecipient().getId();
//        }
//        return chat.getSender().getId();
//    }
//
//
//    @Transactional
//    public void uploadMediaMessage(String chatId, MultipartFile file, Authentication authentication) {
//        Chat chat = chatRepository.findById(chatId)
//                .orElseThrow(() -> new RuntimeException("Chat not found"));
//
//        String senderId = getSenderId(chat, authentication);
//        String receiverId = getRecipientId(chat, authentication);
//
//        String filePath = fileService.saveFile(file, senderId);
//
//
//        Message message = new Message();
//
//        message.setChat(chat);
//        message.setSenderId(senderId);
//        message.setReceiverId(receiverId);
//        message.setType(MessageType.IMAGE);
//        message.setMediaFilePath(filePath);
//
//        message.setChat(chat);
//        message.setState(MessageState.SENT);
//        messageRepository.save(message);
////        messageRepository.save(message);
//
//        Notification notification = Notification.builder()
//                .chatId(chat.getId())
//                .type(NotificationType.IMAGE)
//                .senderId(senderId)
//                .receiverId(receiverId)
//                .messageType(MessageType.IMAGE)
//                .mediaUrl(filePath)
//
//                .build();
//
//        notificationService.sendToUser(receiverId, notification);
////        notificationService.sendToChatTopic(chat.getId(), notification);
//    }
//
//}
//
