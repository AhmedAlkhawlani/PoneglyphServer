//package com.nova.poneglyph.controller;
//
//import com.nova.poneglyph.message.MessageStateUpdate;
//import org.springframework.messaging.handler.annotation.DestinationVariable;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.stereotype.Controller;
//
//@Controller
//public class MessageStateController {
//
//    @MessageMapping("/message-state/{messageId}")
//    @SendTo("/topic/message-state/{messageId}")
//    public MessageStateUpdate handleStateUpdate(
//            @DestinationVariable String messageId,
//            MessageStateUpdate update) {
//
//        System.out.println("Processing state update: {} for {}" +update.getState()+" "+ messageId);
//        return update;
//    }
//}
