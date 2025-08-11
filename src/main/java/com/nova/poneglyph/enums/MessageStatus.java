package com.nova.poneglyph.enums;

public enum MessageStatus {
    SENT,               // تم الإرسال
    DELIVERED,          // تم الوصول للجهاز
    SEEN,               // تمت المشاهدة
    FAILED,             // فشل الإرسال
    PENDING,            // قيد الانتظار (للملفات الكبيرة)
    RECALLED            // تم سحب الرسالة
}
