package com.nova.poneglyph.dto.conversation;

import lombok.Data;

import java.util.List;

@Data
public class CreateConversationRequest {
    private List<String> participantPhones;

    private String initialMessage;
    private String title; // optional for GROUP
    private String type; // DIRECT | GROUP


}
