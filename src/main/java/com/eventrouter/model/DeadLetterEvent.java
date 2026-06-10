package com.eventrouter.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dead_letter_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadLetterEvent
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "original_event_id")
    private UUID originalEventId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    @Column(name = "error_message",
            columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @PrePersist
    public void prePersist() {
        this.failedAt = LocalDateTime.now();
        this.retryCount = 0;
    }
}