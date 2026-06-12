package com.eventrouter.controller;

import com.eventrouter.repository.DeadLetterEventRepository;
import com.eventrouter.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final EventRepository eventRepository;
    private final DeadLetterEventRepository dlqRepository;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {

        long totalEvents = eventRepository.count();
        long alertHandlerCount = eventRepository
                .countByRoutedTo("alert_handler");
        long databaseHandlerCount = eventRepository
                .countByRoutedTo("database_handler");
        long webhookHandlerCount = eventRepository
                .countByRoutedTo("webhook_handler");
        long anomalyCount = eventRepository
                .countByAnomalyFlagTrue();
        long dlqCount = dlqRepository.count();

        return ResponseEntity.ok(Map.of(
                "totalEventsProcessed", totalEvents,
                "anomaliesDetected", anomalyCount,
                "deadLetterCount", dlqCount,
                "routingBreakdown", Map.of(
                        "alert_handler", alertHandlerCount,
                        "database_handler",
                        databaseHandlerCount,
                        "webhook_handler",
                        webhookHandlerCount
                )
        ));
    }
}