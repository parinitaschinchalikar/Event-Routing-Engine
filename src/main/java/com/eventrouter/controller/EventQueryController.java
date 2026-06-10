package com.eventrouter.controller;

import com.eventrouter.model.Event;
import com.eventrouter.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventQueryController {

    private final EventRepository eventRepository;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(
                eventRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(
            @PathVariable UUID id) {
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<Event>> getEventsByType(
            @PathVariable String eventType) {
        return ResponseEntity.ok(
                eventRepository
                        .findByEventTypeOrderByReceivedAtDesc(
                                eventType));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Event>> getEventsByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(
                eventRepository
                        .findByStatusOrderByReceivedAtDesc(
                                status));
    }
}