//package com.nova.poneglyph.fakeChat;
//
//
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.text.SimpleDateFormat;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Component
//public class FakeChatScheduler {
//    private final SimpMessagingTemplate messagingTemplate;
//    private final Random random = new Random();
//    private final Map<String, ConversationState> conversationStates = new HashMap<>();
//
//
//    public FakeChatScheduler(SimpMessagingTemplate messagingTemplate) {
//        this.messagingTemplate = messagingTemplate;
//        initializeConversationStates();
//    }
//
//    private void initializeConversationStates() {
//        // حالة كل شخصية
//        conversationStates.put("1", new ConversationState("Zoro", false, false, true));
//        conversationStates.put("2", new ConversationState("Luffy", false, false, true));
//        conversationStates.put("3", new ConversationState("Nami", false, false, false));
//        conversationStates.put("4", new ConversationState("Sanji", false, false, true));
//        conversationStates.put("5", new ConversationState("Robin", false, false, false));
//    }
//
//    @Scheduled(fixedRate = 3000)
//    public void simulateDynamicConversations() {
//        conversationStates.forEach((userId, state) -> {
//            // 30% فرصة لأي حدث
//            if (random.nextInt(100) < 30) {
//                if (state.isOnline()) {
//                    if (state.isTyping()) {
//                        // إنهاء الكتابة وإرسال الرسالة
//                        sendTypingStatus(userId, state.getName(), false);
//                        sendRandomMessage(userId, state.getName());
//                    } else if (random.nextBoolean()) {
//                        // بدء الكتابة
//                        sendTypingStatus(userId, state.getName(), true);
//                    } else {
//                        // إرسال رسالة مباشرة
//                        sendRandomMessage(userId, state.getName());
//                    }
//                } else {
//                    // 10% فرصة للاتصال
//                    if (random.nextInt(100) < 10) {
//                        updateOnlineStatus(userId, state.getName(), true);
//                    }
//                }
//            }
//        });
//    }
//
//    private void sendRandomMessage(String userId, String sender) {
////        String[] zoroMessages = {"⚔️ من يحارب معي؟", "أحتاج إلى تدريب", "أين لوفي؟"};
//        String[] zoroMessages = {
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
////        String[] luffyMessages = {"☠️ سأكون ملك القراصنة!", "أنا جائع!", "لنذهب في مغامرة!"};
//        String[] luffyMessages = {
//                "☠️ سنكون أقوى قراصنة!",
//                "🍖 أنا جائع جداً!",
//                "🦸‍♂️ لا تخافوا! أنا هنا!",
//                "😂 شانكس سيكون فخوراً بي!",
//                "👑 ملك القراصنة... هذا أنا!",
//                "🤗 أريد أصدقاء جدد!",
//                "💪 المعركة لم تنته بعد!",
//                "🍗 المزيد من اللحم من فضلك!",
//                "😴 بعد الأكل... حان وقت النوم",
//                "🎉 لنحتفل بانتصارنا!"
//        };
////        String[] namiMessages = {"💰 تحتاج إلى المزيد من المال", "الطقس سيء اليوم", "أين زورو؟"};
//        String[] namiMessages = {
//                "💰 ادفع أولاً ثم تحدث!",
//                "🌪️ عاصفة قادمة... استعدوا!",
//                "🗺️ أدرس الخريطة، انتظروني",
//                "👊 زورو! لا تضل الطريق مجدداً!",
//                "🛍️ متى سنذهب للتسوق؟",
//                "🍊 بيلامير... أشتاق إليك",
//                "😠 من سرق أموالي؟!",
//                "🤓 خط السير محسوب بدقة",
//                "⏰ استيقظوا! الوقت ثمين!",
//                "🌞 صباح الخير أيها الكسالى!"
//        };
////        String[] sanjiMessages = {"🍳 الطعام جاهز!", "نيمي-سوان~", "لا تهدر الطعام!"};
//        String[] sanjiMessages = {
//                "🍳 وجبة جديدة جاهزة!",
//                "💘 نيمي-سوان~ روبين-تشان~",
//                "🚬 بعد الطعام... سيجارة هادئة",
//                "👨‍🍳 المطبخ مكاني المقدس",
//                "👞 لا أركل النساء أبداً!",
//                "🍜 وصفة جديدة سأجربها",
//                "🍷 الطعام الجيد يحتاج نبيذاً جيداً",
//                "😤 زورو! نظف أطباقك!",
//                "🍛 طعام اليوم خاص جداً",
//                "💐 هل تريدين زهوراً مع العشاء؟"
//        };
////        String[] robinMessages = {"📚 هل قرأت هذا الكتاب؟", "مثير للاهتمام...", "هذه الهيروغليفية غريبة"};
//
//        String[] robinMessages = {
//                "📖 هذا الكتاب رائع حقاً",
//                "🏛️ هذه الآثار تعود لـ800 عام",
//                "🤔 لغز جديد يحيرني...",
//                "🌺 الزهور تذكرني بأوهارا",
//                "👀 أرى كل شيء بأعيني المغلقة",
//                "🗝️ الحقيقة تكمن في حجر بونيغليف",
//                "😊 لوفي... أنت مضحك حقاً",
//                "🧐 هذه النقوش غير مألوفة",
//                "🌌 الكون يحوي أسراراً لا تحصى",
//                "💡 لدي فكرة... استمعوا إلي"
//        };
//        String message;
//        switch (sender) {
//            case "Zoro": message = zoroMessages[random.nextInt(zoroMessages.length)]; break;
//            case "Luffy": message = luffyMessages[random.nextInt(luffyMessages.length)]; break;
//            case "Nami": message = namiMessages[random.nextInt(namiMessages.length)]; break;
//            case "Sanji": message = sanjiMessages[random.nextInt(sanjiMessages.length)]; break;
//            case "Robin": message = robinMessages[random.nextInt(robinMessages.length)]; break;
//            default: message = "مرحبا!";
//        }
//
//        ChatMessage msg = new ChatMessage(userId, sender, message);
//        System.out.println("💬 [" + sender + "] Sending message: " + message);
//        messagingTemplate.convertAndSend("/topic/messages", msg);
//    }
//
//    // ... الطرق الحالية مع تعديلات ...
//
//
//    // طرق معدلة لاستخدام ConversationState
//    @Scheduled(fixedRate = 15000)
//    public void randomTypingStatus() {
//        conversationStates.forEach((userId, state) -> {
//            if (state.isOnline() && random.nextInt(100) < 40) {
//                boolean typing = random.nextBoolean();
//                state.setTyping(typing);
//                sendTypingStatus(userId, state.getName(), typing);
//            }
//        });
//    }
//
//    @Scheduled(fixedRate = 25000)
//    public void randomRecordingStatus() {
//        conversationStates.forEach((userId, state) -> {
//            if (state.isOnline() && !state.isTyping() && random.nextInt(100) < 20) {
//                boolean recording = random.nextBoolean();
//                state.setRecording(recording);
//                sendRecordingStatus(userId, state.getName(), recording);
//            }
//        });
//    }
//
//    @Scheduled(fixedRate = 45000)
//    public void randomOnlineStatus() {
//        conversationStates.forEach((userId, state) -> {
//            boolean online = random.nextInt(100) < 70; // 70% فرصة أن يكون متصلاً
//            if (state.isOnline() != online) {
//                state.setOnline(online);
//                updateOnlineStatus(userId, state.getName(), online);
//                if (!online) {
//                    sendLastSeen(userId, state.getName(), state.getLastSeen());
//                }
//            }
//        });
//    }
//
//    // طرق الإرسال المساعدة
////    private void sendTypingStatus(String userId, String sender, boolean typing) {
////        TypingStatus status = new TypingStatus(userId, sender, typing);
////        System.out.println(typing ? "✍️ [" + sender + "] is typing" : "✋ [" + sender + "] stopped typing");
////        messagingTemplate.convertAndSend("/topic/typing", status);
////    }
//    // استخدامها في جميع طرق الإرسال بدلاً من System.out.println العادي
//    private void sendTypingStatus(String userId, String sender, boolean typing) {
//        TypingStatus status = new TypingStatus(userId, sender, typing);
//        printWithTimestamp(typing ? "✍️ [" + sender + "] is typing" : "✋ [" + sender + "] stopped typing");
//        messagingTemplate.convertAndSend("/topic/typing", status);
//    }
//
//    private void sendRecordingStatus(String userId, String sender, boolean recording) {
//        RecordingStatus status = new RecordingStatus(userId, sender, recording);
//        System.out.println(recording ? "🎤 [" + sender + "] is recording" : "🔇 [" + sender + "] stopped recording");
//        messagingTemplate.convertAndSend("/topic/recording", status);
//    }
//
//    private void updateOnlineStatus(String userId, String sender, boolean online) {
//        UserStatus status = new UserStatus(userId, sender, online);
//        System.out.println(online ? "🟢 [" + sender + "] is online" : "🔴 [" + sender + "] is offline");
//        messagingTemplate.convertAndSend("/topic/status", status);
//    }
//
//    private void sendLastSeen(String userId, String sender, Date lastSeen) {
//        LastSeenStatus status = new LastSeenStatus(userId, sender, lastSeen);
//        System.out.println("🕓 [" + sender + "] last seen at " + lastSeen);
//        messagingTemplate.convertAndSend("/topic/lastseen", status);
//    }
//
//
//    @Scheduled(fixedRate = 60000)
//    public void simulateGroupConversation() {
//        if (random.nextInt(100) < 50) { // 50% فرصة لحدوث محادثة جماعية
//            String[] participants = conversationStates.keySet().toArray(new String[0]);
//            String initiatorId = participants[random.nextInt(participants.length)];
//            ConversationState initiator = conversationStates.get(initiatorId);
//
//            if (initiator.isOnline()) {
//                String message = "🔊 الجميع: " + getGroupMessage(initiator.getName());
//                ChatMessage msg = new ChatMessage(initiatorId, initiator.getName(), message);
//                System.out.println("👥 [" + initiator.getName() + "] Group message: " + message);
//                messagingTemplate.convertAndSend("/topic/messages", msg);
//
//                // محاكاة ردود الفعل
//                Arrays.stream(participants)
//                        .filter(id -> !id.equals(initiatorId))
//                        .filter(id -> conversationStates.get(id).isOnline())
//                        .forEach(id -> {
//                            if (random.nextInt(100) < 60) { // 60% فرصة للرد
//                                try {
//                                    Thread.sleep(random.nextInt(3000)); // تأخير عشوائي
//                                } catch (InterruptedException e) {
//                                    Thread.currentThread().interrupt();
//                                }
//                                sendRandomMessage(id, conversationStates.get(id).getName());
//                            }
//                        });
//            }
//        }
//    }
//
//    private String getGroupMessage(String sender) {
//        Map<String, String[]> groupMessages = Map.of(
//                "Zoro", new String[]{"لننطلق في مغامرة!", "من يريد التدريب معي؟"},
//                "Luffy", new String[]{"لنأكل معًا!", "هيا نبحث عن الكنز!"},
//                "Nami", new String[]{"لدينا خطة جديدة", "من يستطيع مساعدتي في الملاحة؟"},
//                "Sanji", new String[]{"الطعام جاهز للجميع!", "من جائع؟"},
//                "Robin", new String[]{"لدي اكتشاف جديد", "هل يعرف أحد عن هذه الحضارة؟"}
//        );
//        return groupMessages.getOrDefault(sender, new String[]{"مرحبا جميعا!"})[random.nextInt(2)];
//    }
//    @Scheduled(fixedRate = 120000)
//    public void simulateSpecialEvents() {
//        int event = random.nextInt(5);
//        switch (event) {
//            case 0:
//                // حدث دخول مفاجئ
//                String newUserId = "6";
//                ConversationState newUser = new ConversationState("Shanks", false, false, true);
//                conversationStates.put(newUserId, newUser);
//                updateOnlineStatus(newUserId, "Shanks", true);
//                sendRandomMessage(newUserId, "Shanks");
//                break;
//
//            case 1:
//                // محادثة ساخنة
//                conversationStates.forEach((userId, state) -> {
//                    if (state.isOnline() && random.nextInt(100) < 80) {
//                        sendTypingStatus(userId, state.getName(), true);
//                        try {
//                            Thread.sleep(1000 + random.nextInt(2000));
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt();
//                        }
//                        sendTypingStatus(userId, state.getName(), false);
//                        sendRandomMessage(userId, state.getName());
//                    }
//                });
//                break;
//
//            case 2:
//                // تسجيل صوتي جماعي
//                conversationStates.forEach((userId, state) -> {
//                    if (state.isOnline() && random.nextInt(100) < 50) {
//                        sendRecordingStatus(userId, state.getName(), true);
//                        try {
//                            Thread.sleep(3000 + random.nextInt(5000));
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt();
//                        }
//                        sendRecordingStatus(userId, state.getName(), false);
//                        sendRandomMessage(userId, state.getName());
//                    }
//                });
//                break;
//
//            case 3:
//                // خروج جماعي
//                conversationStates.forEach((userId, state) -> {
//                    if (state.isOnline() && random.nextInt(100) < 70) {
//                        updateOnlineStatus(userId, state.getName(), false);
//                    }
//                });
//                break;
//
//            case 4:
//                // عودة جماعية
//                conversationStates.forEach((userId, state) -> {
//                    if (!state.isOnline() && random.nextInt(100) < 70) {
//                        updateOnlineStatus(userId, state.getName(), true);
//                    }
//                });
//                break;
//        }
//    }
//
//    private void printWithTimestamp(String message) {
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//        System.out.println("[" + sdf.format(new Date()) + "] " + message);
//    }
//
//    private String formatLastSeen(String lastSeen) {
//        if (lastSeen == null || lastSeen.isEmpty()) {
//            return "غير معروف";
//        }
//
//        try {
//            // إذا كان التاريخ عبارة عن timestamp (مثل 1753065403387)
//            if (lastSeen.matches("\\d+")) {
//                long timestamp = Long.parseLong(lastSeen);
//                return formatDateFromTimestamp(timestamp);
//            }
//            // إذا كان التاريخ بصيغة ISO (مثل "2023-07-21T05:36:44.116Z")
//            else {
//                return formatDateFromISO(lastSeen);
//            }
//        } catch (Exception e) {
//            return "آخر ظهور: " + lastSeen; // العودة للقيمة الأصلية في حالة الخطأ
//        }
//    }
//
//    private String formatDateFromTimestamp(long timestamp) {
//        Date date = new Date(timestamp);
//        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//        return "آخر ظهور: " + sdf.format(date);
//    }
//
//    private String formatDateFromISO(String isoDate) {
//        try {
//            Instant instant = Instant.parse(isoDate);
//            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());
//            return "آخر ظهور: " + dateTime.format(formatter);
//        } catch (Exception e) {
//            return isoDate;
//        }
//    }
//}
