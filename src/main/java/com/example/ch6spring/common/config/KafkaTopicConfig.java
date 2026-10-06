package com.example.ch6spring.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration

public class KafkaTopicConfig {

    public static final String ORDER_EVENTS = "order-events";

    @Bean
    public NewTopic orderEventsTopic() {

        return TopicBuilder.name(ORDER_EVENTS)
                .partitions(3)
                .replicas(3)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                .build();
    }

}
