package com.eventrouter.service;

import com.eventrouter.dto.EventPayload;
import com.eventrouter.model.Event;
import com.eventrouter.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventConsumerService {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topic.raw}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "3"
    )
    public void consume(ConsumerRecord<String, String> record) {
        log.info("Received event: key={} partition={} offset={}",
                record.key(),
                record.partition(),
                record.offset());

        try {
            // Parse the incoming JSON payload
            EventPayload payload = objectMapper.readValue(
                    record.value(), EventPayload.class);

            log.info("Parsed event: type={} source={}",
                    payload.getEventType(),
                    payload.getSourceSystem());

            // Save raw event to DB with status 'received'
            Event event = Event.builder()
                    .eventType(payload.getEventType())
                    .payload(record.value())
                    .status("received")
                    .build();

            eventRepository.save(event);

            log.info("Event saved to DB: id={} type={}",
                    event.getId(),
                    event.getEventType());

        } catch (Exception e) {
            log.error("Failed to process event: key={} error={}",
                    record.key(), e.getMessage(), e);
        }
    }
}