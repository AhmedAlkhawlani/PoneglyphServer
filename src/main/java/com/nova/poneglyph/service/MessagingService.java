package com.nova.poneglyph.service;

import com.nova.poneglyph.domain.conversation.Conversation;
import com.nova.poneglyph.domain.enums.MessageType;
import com.nova.poneglyph.domain.message.Message;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessagingService {
    private final MessageRepository messageRepo;

    @Transactional
    public Message sendText(Conversation c, User sender, String cipherBase64) {
        Message m = Message.builder()
                .conversation(c)
                .sender(sender)
                .messageType(MessageType.TEXT)
                .encryptedContent(java.util.Base64.getDecoder().decode(cipherBase64))
                .build();
        m.setId(UUID.randomUUID());
        m.setDeletedForAll(false);
        m.setDeletedAt(null);
        messageRepo.save(m);
        // sequence_number set by DB; consider refreshing if needed
        return m;
    }
}
