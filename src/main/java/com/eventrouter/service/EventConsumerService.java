package com.eventrouter.service;

import com.eventrouter.dto.EnrichmentResult;
import com.eventrouter.dto.EventPayload;
import com.eventrouter.model.Event;
import com.eventrouter.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventConsumerService {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final LlmEnrichmentService llmEnrichmentService;
    private final RoutingService routingService;

    @KafkaListener(
            topics = "${kafka.topic.raw}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "3"
    )
    public void consume(
            ConsumerRecord<String, String> record) {

        log.info("Received event: key={} partition={}" +
                        " offset={}",
                record.key(),
                record.partition(),
                record.offset());

        try {
            // Step 1 — Parse payload
            EventPayload payload = objectMapper.readValue(
                    record.value(), EventPayload.class);

            log.info("Parsed event: type={} source={}",
                    payload.getEventType(),
                    payload.getSourceSystem());

            // Step 2 — Save with status 'received'
            Event event = Event.builder()
                    .eventType(payload.getEventType())
                    .payload(record.value())
                    .status("received")
                    .build();
            eventRepository.save(event);

            log.info("Event saved: id={} type={}",
                    event.getId(),
                    event.getEventType());

            // Step 3 — Enrich with LLM
            log.info("Enriching event with LLM...");
            EnrichmentResult enrichment =
                    llmEnrichmentService.enrichEvent(
                            payload.getEventType(),
                            record.value());

            // Step 4 — Update with enrichment
            event.setRiskScore(enrichment.getRiskScore());
            event.setAnomalyFlag(enrichment.getAnomalyFlag());
            event.setRecommendedAction(
                    enrichment.getRecommendedAction());
            event.setStatus("enriched");
            event.setProcessedAt(LocalDateTime.now());
            eventRepository.save(event);

            log.info("Event enriched: id={} riskScore={}" +
                            " anomaly={} action={}",
                    event.getId(),
                    enrichment.getRiskScore(),
                    enrichment.getAnomalyFlag(),
                    enrichment.getRecommendedAction());

            // Step 5 — Route event
            log.info("Routing event...");
            routingService.routeEvent(event);

        } catch (Exception e) {
            log.error("Failed to process event: key={}" +
                            " error={}",
                    record.key(),
                    e.getMessage(), e);
        }
    }
}