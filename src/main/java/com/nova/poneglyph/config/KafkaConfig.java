package com.nova.poneglyph.config;//package com.nova.poneglyph.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
//    @Value("${spring.kafka.topic.name}")
//    private String topicName;
//
//    @Bean
//    public NewTopic topic() {
//        return TopicBuilder.name(topicName)
//                .build();
//    }


//    @Bean
//    public NewTopic connectionEventsTopic() {
//        return TopicBuilder.name("connection-events")
//                .partitions(3)
//                .config(TopicConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName())
//                .build();
//    }


    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name("notifications-topic")
                .partitions(3)
                .replicas(2)
                .build();
    }

    @Bean
    public NewTopic pendingNotificationsTopic() {
        return TopicBuilder.name("pending-notifications")
                .partitions(3)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 أيام
                .build();
    }

    @Bean
    public NewTopic connectionEventsTopic() {
        return TopicBuilder.name("connection-events")
                .partitions(3)
                .build();
    }
}

