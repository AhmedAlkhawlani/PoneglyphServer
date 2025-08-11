//package com.nova.poneglyph.controller;
//
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class KafkaProducer {
//
//    @Value("${spring.kafka.topic.name}")
//    private String topic;
//
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//
//    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    public void sendNotification(NotificationDto notificationDto) {
//        kafkaTemplate.send(topic, notificationDto);
//    }
//}
