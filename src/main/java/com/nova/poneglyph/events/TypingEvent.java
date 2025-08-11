package com.nova.poneglyph.events;

public class TypingEvent {

    private String conversationId;
    private String senderPhone;
    private String receiverPhone;
    private boolean typing; // ✅ بدون event داخلها

    public TypingEvent(String conversationId, String senderPhone, String receiverPhone, boolean isTyping) {
        this.conversationId = conversationId;
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.typing = isTyping;
    }


    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }


    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }
}
