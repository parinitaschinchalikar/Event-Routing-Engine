package com.eventrouter.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig
{
    @Value("${kafka.topic.raw}")
    private String rawTopic;

    @Value("${kafka.topic.dlq}")
    private String dlqTopic;

    @Bean
    public NewTopic rawEventsTopic()
    {
        return TopicBuilder
                .name(rawTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deadLetterTopic()
    {
        return TopicBuilder
                .name(dlqTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}