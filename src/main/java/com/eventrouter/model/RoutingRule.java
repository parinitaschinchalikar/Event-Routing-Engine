package com.eventrouter.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "routing_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "condition_field", nullable = false)
    private String conditionField;

    @Column(name = "condition_operator", nullable = false)
    private String conditionOperator;

    @Column(name = "condition_value", nullable = false)
    private String conditionValue;

    @Column(name = "handler", nullable = false)
    private String handler;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }
}