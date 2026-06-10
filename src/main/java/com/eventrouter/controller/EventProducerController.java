package com.eventrouter.controller;

import com.eventrouter.dto.EventPayload;
import com.eventrouter.dto.PublishEventRequest;
import com.eventrouter.dto.PublishEventResponse;
import com.eventrouter.service.EventProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventProducerController
{
    private final EventProducerService producerService;

    // Publish a custom event
    @PostMapping("/publish")
    public ResponseEntity<PublishEventResponse> publish(
            @RequestBody PublishEventRequest request) {

        EventPayload payload = EventPayload.builder()
                .eventType(request.getEventType())
                .sourceSystem("api")
                .data(request.getData())
                .build();

        String eventId = producerService
                .publishEvent(payload);

        return ResponseEntity.ok(
                PublishEventResponse.builder()
                        .eventId(eventId)
                        .eventType(request.getEventType())
                        .status("published")
                        .message("Event sent to Kafka")
                        .timestamp(
                                System.currentTimeMillis())
                        .build()
        );
    }

    // Simulate a payment_failed event
    @PostMapping("/simulate/payment-failed")
    public ResponseEntity<PublishEventResponse> simulatePaymentFailed() {
        EventPayload payload = EventPayload.builder()
                .eventType("payment_failed")
                .sourceSystem("payment-service")
                .data(Map.of(
                        "orderId", "ORD-" + (int)(Math.random() * 9000 + 1000),
                        "amount", 249.99,
                        "customerId", "C-" + (int)(Math.random() * 900 + 100),
                        "attemptCount", 3,
                        "currency", "USD"
                ))
                .build();

        String eventId = producerService.publishEvent(payload);

        return ResponseEntity.ok(
                PublishEventResponse.builder()
                        .eventId(eventId)
                        .eventType("payment_failed")
                        .status("published")
                        .message("Simulated payment_failed event sent")
                        .timestamp(System.currentTimeMillis())
                        .build()
        );
    }

    // Simulate an order_placed event
    @PostMapping("/simulate/order-placed")
    public ResponseEntity<PublishEventResponse> simulateOrderPlaced() {
        EventPayload payload = EventPayload.builder()
                .eventType("order_placed")
                .sourceSystem("order-service")
                .data(Map.of(
                        "orderId", "ORD-" + (int)(Math.random() * 9000 + 1000),
                        "amount", 89.50,
                        "customerId", "C-" + (int)(Math.random() * 900 + 100),
                        "items", 3,
                        "shippingCountry", "US"
                ))
                .build();

        String eventId = producerService.publishEvent(payload);

        return ResponseEntity.ok(
                PublishEventResponse.builder()
                        .eventId(eventId)
                        .eventType("order_placed")
                        .status("published")
                        .message("Simulated order_placed event sent")
                        .timestamp(System.currentTimeMillis())
                        .build()
        );
    }

    // Simulate a user_signup event
    @PostMapping("/simulate/user-signup")
    public ResponseEntity<PublishEventResponse> simulateUserSignup() {
        EventPayload payload = EventPayload.builder()
                .eventType("user_signup")
                .sourceSystem("auth-service")
                .data(Map.of(
                        "userId", "U-" + (int)(Math.random() * 9000 + 1000),
                        "email", "user" + (int)(Math.random() * 999) + "@example.com",
                        "country", "US",
                        "signupMethod", "email"
                ))
                .build();

        String eventId = producerService.publishEvent(payload);

        return ResponseEntity.ok(
                PublishEventResponse.builder()
                        .eventId(eventId)
                        .eventType("user_signup")
                        .status("published")
                        .message("Simulated user_signup event sent")
                        .timestamp(System.currentTimeMillis())
                        .build()
        );
    }

    // Simulate a system_alert event
    @PostMapping("/simulate/system-alert")
    public ResponseEntity<PublishEventResponse> simulateSystemAlert() {
        EventPayload payload = EventPayload.builder()
                .eventType("system_alert")
                .sourceSystem("monitoring-service")
                .data(Map.of(
                        "alertId", "ALT-" + (int)(Math.random() * 9000 + 1000),
                        "severity", "HIGH",
                        "service", "payment-gateway",
                        "message", "Response time exceeded threshold"
                ))
                .build();

        String eventId = producerService.publishEvent(payload);

        return ResponseEntity.ok(
                PublishEventResponse.builder()
                        .eventId(eventId)
                        .eventType("system_alert")
                        .status("published")
                        .message("Simulated system_alert event sent")
                        .timestamp(System.currentTimeMillis())
                        .build()
        );
    }
}