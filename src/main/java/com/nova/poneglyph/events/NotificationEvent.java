package com.nova.poneglyph.events;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.nova.poneglyph.dto.MessageDTO;
import com.nova.poneglyph.dto.PresenceDTO;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    private String notificationId;   // UUID لكل إشعار
    private String userId;           // المستخدم المستهدف
    private EventType eventType;     // نوع الحدث

    // بيانات الرسالة (MESSAGE, DELIVERY, SEEN)
    private MessageDTO message;

    // بيانات التحديث على المحادثة
    private ConversationUpdateEvent conversationUpdate;

    // بيانات الحالة (PresenceDTO)
    private PresenceDTO presence;

    // نص عام أو تنبيه SYSTEM/ALERT
    private String systemMessage;

    // مرتبط برسالة محددة إن وجد
    private String relatedMessageId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public enum EventType {
        MESSAGE,
        DELIVERY,
        SEEN,
        CONVERSATION_UPDATE,
        PRESENCE,
        SYSTEM,
        ALERT,
        TYPING
    }
    /*
    *     MESSAGE,      // رسالة نصية أو وسائط
    DELIVERY,     // تم التسليم
    SEEN,         // تم القراءة
    PRESENCE,     // حالة الاتصال
    TYPING,       // يكتب الآن
    SYSTEM,       // رسائل النظام
    ALERT         // تنبيه خاص*/

}

