//package com.nova.poneglyph.fakeChat;
//
//import java.util.Date;
//
//public class ConversationState {
//    private final String name;
//    private boolean isTyping;
//    private boolean isRecording;
//    private boolean isOnline;
//    private Date lastSeen;
//
//    public ConversationState(String name, boolean isTyping, boolean isRecording, boolean isOnline) {
//        this.name = name;
//        this.isTyping = isTyping;
//        this.isRecording = isRecording;
//        this.isOnline = isOnline;
//        this.lastSeen = new Date();
//    }
//
//    // Getters and Setters
//    public String getName() { return name; }
//    public boolean isTyping() { return isTyping; }
//    public boolean isRecording() { return isRecording; }
//    public boolean isOnline() { return isOnline; }
//    public Date getLastSeen() { return lastSeen; }
//
//    public void setTyping(boolean typing) { isTyping = typing; }
//    public void setRecording(boolean recording) { isRecording = recording; }
//    public void setOnline(boolean online) {
//        isOnline = online;
//        if (!online) {
//            lastSeen = new Date();
//        }
//    }
//}
