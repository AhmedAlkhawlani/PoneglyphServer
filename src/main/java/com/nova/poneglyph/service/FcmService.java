//package com.nova.poneglyph.service;
//
//import com.google.firebase.messaging.*;
//import com.nova.poneglyph.domain.message.Message;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import javax.management.Notification;
//import java.util.concurrent.ExecutionException;
//
//@Service
//@RequiredArgsConstructor
//public class FcmService {
//
//    public void sendNotification(String deviceToken, String title, String body) {
//        Notification notification = Notification.builder()
//                .setTitle(title)
//                .setBody(body)
//                .build();
//
//        Message message = Message.builder()
//                .setToken(deviceToken)
//                .setNotification(notification)
//                .build();
//
//        try {
//            String response = FirebaseMessaging.getInstance().sendAsync(message).get();
//            System.out.println("✅ Sent FCM message: " + response);
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException("❌ Failed to send FCM message", e);
//        }
//    }
//}
//
