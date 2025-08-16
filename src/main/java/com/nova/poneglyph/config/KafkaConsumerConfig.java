package com.nova.poneglyph.config;


 import com.nova.poneglyph.events.NotificationEvent;
 import org.apache.kafka.clients.consumer.ConsumerConfig;
 import org.apache.kafka.clients.consumer.KafkaConsumer;
 import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
 import org.springframework.kafka.support.serializer.JsonSerializer;

 import java.util.HashMap;
import java.util.Map;
 import java.util.Properties;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

//    @Bean
//    public ConsumerFactory<String, NotificationEvent> notificationConsumerFactory() {
//        JsonDeserializer<NotificationEvent> deserializer = new JsonDeserializer<>(NotificationEvent.class);
//        deserializer.setRemoveTypeHeaders(false);
//        deserializer.addTrustedPackages("*");  // يضمن قبول أي حزمة
//        deserializer.setUseTypeMapperForKey(true);
//
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // مهم جداً أيضاً
//        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
//
//        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> kafkaListenerContainerFactory() {
//        JsonDeserializer<NotificationEvent> deserializer = new JsonDeserializer<>(NotificationEvent.class);
//        deserializer.addTrustedPackages("*"); // لقبول أي حزمة
//
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-group");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        // لا تضع VALUE_DESERIALIZER_CLASS_CONFIG هنا
//        // props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//
//        DefaultKafkaConsumerFactory<String, NotificationEvent> factory =
//                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
//
//        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> containerFactory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        containerFactory.setConsumerFactory(factory);
//
//        return containerFactory;
//    }
//


    @Bean
    public ConsumerFactory<String, NotificationEvent> notificationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(NotificationEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory());
        return factory;
    }
    @Bean
    public KafkaConsumer<String, NotificationEvent> pendingNotificationsConsumer(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "pending-resender");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return new KafkaConsumer<>(props);
    }
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(
//                new DefaultKafkaConsumerFactory<>(
//                        Map.of(
//                                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
//                                ConsumerConfig.GROUP_ID_CONFIG, "chat-group",
//                                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
//                                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
//                        )
//                )
//        );
//        return factory;
//    }

}


