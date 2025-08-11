package com.nova.poneglyph.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartConversationRequest {
    @NotBlank(message = "Participant 1 phone number is required")
    private String participant1Phone;

    @NotBlank(message = "Participant 2 phone number is required")
    private String participant2Phone;
}
