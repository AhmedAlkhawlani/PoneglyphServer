package com.nova.poneglyph.dto;



import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingEventRequest {
    @NotBlank
    private String conversationId;

    @NotBlank
    private String senderPhone;

    @NotBlank
    private String receiverPhone;

    private boolean typing; // ✅ بدون "is" في الاسم
}
