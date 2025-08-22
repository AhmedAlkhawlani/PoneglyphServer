package com.nova.poneglyph.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettings {
    private Boolean callEnabled;
    private Boolean messageEnabled;
    private Boolean mentionEnabled;
}

/// كائن NotificationSettings (يمثل الإعدادات من jsonb)
/// بدل ما تخزن النصوص كـ String وتفكها يدويًا، الأفضل تعمل كلاس يمثّل الـ JSON وتحوّله باستخدام Jackson.
