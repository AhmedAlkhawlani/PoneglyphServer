package com.nova.poneglyph.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    @NotBlank
    private String conversationId;

    @NotBlank
    private String senderPhone;

    @NotBlank
    private String receiverPhone;

    @NotBlank
    private String content;
    private String replyToId; // إضافة حقل الرد

}
