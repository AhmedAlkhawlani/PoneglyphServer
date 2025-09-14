////package com.nova.poneglyph.service.chat;
////
////
////
////import com.nova.poneglyph.domain.conversation.Conversation;
////import com.nova.poneglyph.domain.conversation.Participant;
////import com.nova.poneglyph.domain.enums.ConversationType;
////import com.nova.poneglyph.domain.enums.ParticipantRole;
////import com.nova.poneglyph.domain.user.User;
////import com.nova.poneglyph.dto.conversation.MessageDTO;
////import com.nova.poneglyph.repository.ConversationRepository;
////import com.nova.poneglyph.repository.ParticipantRepository;
////import com.nova.poneglyph.repository.UserRepository;
////import com.nova.poneglyph.service.WebSocketService;
////import jakarta.annotation.PostConstruct;
////import lombok.RequiredArgsConstructor;
////import org.springframework.scheduling.annotation.Scheduled;
////import org.springframework.stereotype.Service;
////
////
////import java.time.OffsetDateTime;
////import java.util.Optional;
////import java.util.Random;
////import java.util.UUID;
////
////
////@Service
////@RequiredArgsConstructor
////public class FakeChatScheduler {
////
////    private final WebSocketService webSocketService;
////    private final UserRepository userRepository;
////    private final ConversationRepository conversationRepository;
////    private final ParticipantRepository participantRepository;
////
////    private final Random random = new Random();
////    private UUID fakeUserId;
////    private UUID fakeConversationId;
////    private final UUID yourUserId = UUID.fromString("9f698235-8cf3-4d51-9b97-98207e21d258");
////
////    // حالات المحادثة الوهمية
////    private boolean isOnline = false;
////    private boolean isTyping = false;
////
////    @PostConstruct
////    public void init() {
////        // استخدام المستخدم الموجود بدلاً من إنشاء جديد
////        fakeUserId = UUID.fromString("48a2c29d-8710-4754-b1d8-c814c79084c0");
////
////        // تحديث اسم العرض للمستخدم ليصبح "زورو"
////        User fakeUser = userRepository.findById(fakeUserId)
////                .orElseThrow(() -> new RuntimeException("المستخدم الوهمي غير موجود"));
////
////        // يمكنك تحديث البروفايل إذا أردت
////        // fakeUser.setDisplayName("زورو");
////        // userRepository.save(fakeUser);
////
////        // إنشاء أو العثور على المحادثة المباشرة بينك وبين المستخدم الوهمي
////        Optional<Conversation> existingConversation = conversationRepository.findDirectConversation(yourUserId, fakeUserId);
////        if (existingConversation.isPresent()) {
////            fakeConversationId = existingConversation.get().getId();
////            System.out.println("fakeConversationId :" +fakeConversationId);
////
////        } else {
////            Conversation newConversation = new Conversation();
////            newConversation.setId(UUID.randomUUID());
////            newConversation.setType(ConversationType.DIRECT);
////            newConversation.setEncrypted(false);
////            newConversation.setLastMessageAt(OffsetDateTime.now());
////            conversationRepository.save(newConversation);
////            fakeConversationId = newConversation.getId();
////
////            // إضافة المشاركين
////            addParticipant(newConversation, yourUserId, ParticipantRole.MEMBER);
////            addParticipant(newConversation, fakeUserId, ParticipantRole.MEMBER);
////        }
////
////        // بدء المحاكاة بعد تأخير بسيط
////        new java.util.Timer().schedule(
////                new java.util.TimerTask() {
////                    @Override
////                    public void run() {
////                        isOnline = true;
////                        updateOnlineStatus(true);
////                    }
////                },
////                5000
////        );
////    }
////
////    private void addParticipant(Conversation conversation, UUID userId, ParticipantRole role) {
////        User user = userRepository.findById(userId).orElseThrow();
////        Participant participant = new Participant();
////        participant.setId(UUID.randomUUID());
////        participant.setConversation(conversation);
////        participant.setUser(user);
////        participant.setRole(role);
////        participant.setJoinedAt(OffsetDateTime.now());
////        participant.setUnreadCount(0);
////        participantRepository.save(participant);
////    }
////
////    @Scheduled(fixedRate = 3000)
////    public void simulateChat() {
////        if (!isOnline) return;
////
////        if (random.nextInt(100) < 30) {
////            if (isTyping) {
////                sendTypingStatus(false);
////                sendRandomMessage();
////            } else if (random.nextBoolean()) {
////                sendTypingStatus(true);
////            } else {
////                sendRandomMessage();
////            }
////        }
////    }
////
////    @Scheduled(fixedRate = 15000)
////    public void simulateTyping() {
////        if (!isOnline) return;
////
////        if (random.nextInt(100) < 40) {
////            isTyping = random.nextBoolean();
////            sendTypingStatus(isTyping);
////        }
////    }
////
////    @Scheduled(fixedRate = 25000)
////    public void simulateOnlineStatus() {
////        boolean newOnlineStatus = random.nextInt(100) < 70;
////        if (isOnline != newOnlineStatus) {
////            isOnline = newOnlineStatus;
////            updateOnlineStatus(newOnlineStatus);
////        }
////    }
////
////    private void sendRandomMessage() {
////        String[] messages = {
////                "⚔️ من يجرؤ على تحديي؟",
////                "💪 التدريب لا يتوقف أبداً!",
////                "🗡️ طريق الساموراي لا يعرف التراجع",
////                "😤 لوفي! توقف عن الضياع!",
////                "🍶 أيها المطعم... أين أنت؟",
////                "🤕 آه... هذا الجرح مزعج",
////                "🧭 أنا لست ضائعاً، أنا أستكشف!",
////                "💤 التدريب الشاق يستحق قيلولة جيدة",
////                "👊 لن تهزمنا أي قراصنة!",
////                "🗿 الصبر مفتاح القوة الحقيقية"
////        };
////
////        String messageContent = messages[random.nextInt(messages.length)];
////
////        MessageDTO messageDTO = new MessageDTO();
////        messageDTO.setId(UUID.randomUUID());
////        messageDTO.setConversationId(fakeConversationId);
////        messageDTO.setSenderId(fakeUserId);
////        messageDTO.setSenderName("زورو"); // يمكنك استخدام الاسم من قاعدة البيانات إذا أردت
////        messageDTO.setMessageType("TEXT");
////        messageDTO.setContent(messageContent);
////        messageDTO.setCreatedAt(OffsetDateTime.now());
////        messageDTO.setLocalId(UUID.randomUUID().toString());
////
////        System.out.println("💬 [زورو] sending message: " + messageContent);
////        webSocketService.notifyNewMessage(fakeConversationId, messageDTO);
////    }
////
////    private void sendTypingStatus(boolean typing) {
////        System.out.println(typing ? "✍️ [زورو] is typing" : "✋ [زورو] stopped typing");
////        webSocketService.notifyUserStatusChange(fakeUserId, typing ? "typing" : "online");
////    }
////
////    private void updateOnlineStatus(boolean online) {
////        System.out.println(online ? "🟢 [زورو] is online" : "🔴 [زورو] is offline");
////        webSocketService.notifyPresenceChange(fakeUserId, online);
////        if (!online) {
////            isTyping = false;
////        }
////    }
////
////    // طريقة للحصول على معرف المحادثة الوهمية
////    public UUID getFakeConversationId() {
////        return fakeConversationId;
////    }
////}
//package com.nova.poneglyph.service.chat;
//
//import com.nova.poneglyph.domain.conversation.Conversation;
//import com.nova.poneglyph.domain.conversation.Participant;
//import com.nova.poneglyph.domain.enums.ConversationType;
//import com.nova.poneglyph.domain.enums.ParticipantRole;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.dto.conversation.MessageDTO;
//import com.nova.poneglyph.dto.websocket.TypingIndicator;
//import com.nova.poneglyph.repository.ConversationRepository;
//import com.nova.poneglyph.repository.ParticipantRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.service.WebSocketService;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//
//import java.time.OffsetDateTime;
//import java.util.Optional;
//import java.util.Random;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class FakeChatScheduler {
//
//    private final WebSocketService webSocketService;
//    private final UserRepository userRepository;
//    private final ConversationRepository conversationRepository;
//    private final ParticipantRepository participantRepository;
//
//    private final Random random = new Random();
//    private UUID fakeUserId;
//    private UUID fakeConversationId;
//    private final UUID yourUserId = UUID.fromString("9f698235-8cf3-4d51-9b97-98207e21d258");
//
//    // حالات المحادثة الوهمية
//    private boolean isOnline = false;
//    private boolean isTyping = false;
//    private boolean isInitialized = false;
//
//    @PostConstruct
//    public void init() {
//        try {
//            // استخدام المستخدم الموجود بدلاً من إنشاء جديد
//            fakeUserId = UUID.fromString("48a2c29d-8710-4754-b1d8-c814c79084c0");
//
//            // التحقق من وجود المستخدم
//            Optional<User> fakeUserOpt = userRepository.findById(fakeUserId);
//            if (fakeUserOpt.isEmpty()) {
//                System.out.println("❌ المستخدم الوهمي غير موجود: " + fakeUserId);
//                return;
//            }
//
//            // إنشاء أو العثور على المحادثة المباشرة بينك وبين المستخدم الوهمي
//            Optional<Conversation> existingConversation = conversationRepository.findDirectConversation(yourUserId, fakeUserId);
//            if (existingConversation.isPresent()) {
//                fakeConversationId = existingConversation.get().getId();
//                System.out.println("✅ وجدت محادثة موجودة: " + fakeConversationId);
//            } else {
//                Conversation newConversation = new Conversation();
//                newConversation.setId(UUID.randomUUID());
//                newConversation.setType(ConversationType.DIRECT);
//                newConversation.setEncrypted(false);
//                newConversation.setLastMessageAt(OffsetDateTime.now());
//                conversationRepository.save(newConversation);
//                fakeConversationId = newConversation.getId();
//
//                // إضافة المشاركين
//                addParticipant(newConversation, yourUserId, ParticipantRole.MEMBER);
//                addParticipant(newConversation, fakeUserId, ParticipantRole.MEMBER);
//                System.out.println("✅ أنشأت محادثة جديدة: " + fakeConversationId);
//            }
//
//            isInitialized = true;
//
//            // بدء المحاكاة بعد تأخير بسيط
//            new java.util.Timer().schedule(
//                    new java.util.TimerTask() {
//                        @Override
//                        public void run() {
//                            isOnline = true;
//                            updateOnlineStatus(true);
//                            System.out.println("🚀 بدء محاكاة المحادثة مع زورو");
//                        }
//                    },
//                    3000 // 3 ثواني بدلاً من 5
//            );
//
//        } catch (Exception e) {
//            System.out.println("❌ خطأ في تهيئة المحادثة الوهمية: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    private void addParticipant(Conversation conversation, UUID userId, ParticipantRole role) {
//        try {
//            User user = userRepository.findById(userId).orElseThrow();
//            Participant participant = new Participant();
//            participant.setId(UUID.randomUUID());
//            participant.setConversation(conversation);
//            participant.setUser(user);
//            participant.setRole(role);
//            participant.setJoinedAt(OffsetDateTime.now());
//            participant.setUnreadCount(0);
//            participantRepository.save(participant);
//        } catch (Exception e) {
//            System.out.println("❌ خطأ في إضافة مشارك: " + e.getMessage());
//        }
//    }
//
//    @Scheduled(fixedRate = 3000)
//    public void simulateChat() {
//        if (!isInitialized || !isOnline) return;
//
//        if (random.nextInt(100) < 30) {
//            if (isTyping) {
//                sendTypingStatus(false);
//                sendRandomMessage();
//            } else if (random.nextBoolean()) {
//                sendTypingStatus(true);
//            } else {
//                sendRandomMessage();
//            }
//        }
//    }
//
//    @Scheduled(fixedRate = 15000)
//    public void simulateTyping() {
//        if (!isInitialized || !isOnline) return;
//
//        if (random.nextInt(100) < 40) {
//            isTyping = random.nextBoolean();
//            sendTypingStatus(isTyping);
//        }
//    }
//
//    @Scheduled(fixedRate = 25000)
//    public void simulateOnlineStatus() {
//        if (!isInitialized) return;
//
//        boolean newOnlineStatus = random.nextInt(100) < 70;
//        if (isOnline != newOnlineStatus) {
//            isOnline = newOnlineStatus;
//            updateOnlineStatus(newOnlineStatus);
//        }
//    }
//
//    private void sendRandomMessage() {
//        String[] messages = {
//                "⚔️ من يجرؤ على تحديي؟",
//                "💪 التدريب لا يتوقف أبداً!",
//                "🗡️ طريق الساموراي لا يعرف التراجع",
//                "😤 لوفي! توقف عن الضياع!",
//                "🍶 أيها المطعم... أين أنت؟",
//                "🤕 آه... هذا الجرح مزعج",
//                "🧭 أنا لست ضائعاً، أنا أستكشف!",
//                "💤 التدريب الشاق يستحق قيلولة جيدة",
//                "👊 لن تهزمنا أي قراصنة!",
//                "🗿 الصبر مفتاح القوة الحقيقية"
//        };
//
//        String messageContent = messages[random.nextInt(messages.length)];
//
//        MessageDTO messageDTO = new MessageDTO();
//        messageDTO.setId(UUID.randomUUID());
//        messageDTO.setConversationId(fakeConversationId);
//        messageDTO.setSenderId(fakeUserId);
//        messageDTO.setSenderName("زورو");
//        messageDTO.setMessageType("TEXT");
//        messageDTO.setContent(messageContent);
//        messageDTO.setCreatedAt(OffsetDateTime.now());
//        messageDTO.setLocalId(UUID.randomUUID().toString());
//
//        System.out.println("💬 [زورو] إرسال رسالة: " + messageContent);
//        webSocketService.notifyNewMessage(fakeConversationId, messageDTO);
//    }
//
//    private void sendTypingStatus(boolean typing) {
//        System.out.println(typing ? "✍️ [زورو] يكتب..." : "✋ [زورو] توقف عن الكتابة");
//
//        TypingIndicator typingIndicator = new TypingIndicator();
//        typingIndicator.setTyping(typing);
//        typingIndicator.setUserId(fakeUserId);
//        typingIndicator.setConversationId(fakeConversationId);
//        typingIndicator.setTimestamp(System.currentTimeMillis());
//        webSocketService.notifyTyping(fakeConversationId,typingIndicator);
////        webSocketService.notifyUserStatusChange(fakeUserId, typing ? "typing" : "online");
//
//    }
//
//    private void updateOnlineStatus(boolean online) {
//        System.out.println(online ? "🟢 [زورو] متصل" : "🔴 [زورو] غير متصل");
//        webSocketService.notifyPresenceChange(fakeUserId, online,"away");
//
//        if (!online) {
//            isTyping = false;
//        }
//    }
//
//    // طريقة للحصول على معرف المحادثة الوهمية
//    public UUID getFakeConversationId() {
//        return fakeConversationId;
//    }
//
//    // طريقة لتمكين/تعطيل المحاكاة
//    public void setSimulationEnabled(boolean enabled) {
//        if (!enabled) {
//            isOnline = false;
//            isTyping = false;
//            updateOnlineStatus(false);
//        } else if (!isOnline) {
//            isOnline = true;
//            updateOnlineStatus(true);
//        }
//    }
//}
