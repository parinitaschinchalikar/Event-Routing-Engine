package com.eventrouter.service;

import com.eventrouter.model.DeadLetterEvent;
import com.eventrouter.repository.DeadLetterEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DeadLetterEventRepository dlqRepository;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.dlq}")
    private String dlqTopic;

    public void sendToDeadLetter(String eventKey,
                                 String eventType,
                                 String payload,
                                 String errorMessage) {
        try {
            // Save to DB
            DeadLetterEvent dlqEvent = DeadLetterEvent
                    .builder()
                    .originalEventId(
                            UUID.fromString(eventKey))
                    .eventType(eventType)
                    .payload(payload)
                    .errorMessage(errorMessage)
                    .build();

            dlqRepository.save(dlqEvent);

            // Also publish to DLQ Kafka topic
            kafkaTemplate.send(dlqTopic,
                    eventKey, payload);

            log.warn("Event sent to DLQ: key={} type={}" +
                            " error={}",
                    eventKey, eventType, errorMessage);

        } catch (Exception e) {
            log.error("Failed to send to DLQ: {}",
                    e.getMessage());
        }
    }
}