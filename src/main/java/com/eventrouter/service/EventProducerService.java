package com.eventrouter.service;

import com.eventrouter.dto.EventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.raw}")
    private String rawTopic;

    public String publishEvent(EventPayload payload) {
        try {
            String eventId = UUID.randomUUID().toString();
            payload.setTimestamp(System.currentTimeMillis());

            String message = objectMapper
                    .writeValueAsString(payload);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(rawTopic, eventId, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Event published: type={} id={} " +
                                    "partition={} offset={}",
                            payload.getEventType(),
                            eventId,
                            result.getRecordMetadata()
                                    .partition(),
                            result.getRecordMetadata()
                                    .offset());
                } else {
                    log.error("Failed to publish event: " +
                                    "type={} error={}",
                            payload.getEventType(),
                            ex.getMessage());
                }
            });

            return eventId;

        } catch (Exception e) {
            log.error("Error serializing event: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Failed to publish event: "
                            + e.getMessage());
        }
    }
}