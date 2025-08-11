package com.nova.poneglyph.events;

import com.nova.poneglyph.dto.MessageDTO;

import java.util.List;


public class ConversationUpdateEvent {
    private String conversationId;
    private MessageDTO lastMessage;
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

    public MessageDTO getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageDTO lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
