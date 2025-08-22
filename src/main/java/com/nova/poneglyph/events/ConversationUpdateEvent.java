package com.nova.poneglyph.events;



import com.nova.poneglyph.dto.chatDto.MessageDto;

import java.util.List;


public class ConversationUpdateEvent {
    private String conversationId;
    private MessageDto lastMessage;
    private int unreadCount;
    private List<String> participantIds; // إضافة هذا الحقل

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public MessageDto getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageDto lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
