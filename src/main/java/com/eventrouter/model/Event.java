package com.eventrouter.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", columnDefinition = "jsonb",
            nullable = false)
    private String payload;

    @Column(name = "risk_score",
            precision = 3, scale = 2)
    private BigDecimal riskScore;

    @Column(name = "anomaly_flag")
    private Boolean anomalyFlag;

    @Column(name = "recommended_action")
    private String recommendedAction;

    @Column(name = "routed_to")
    private String routedTo;

    @Column(name = "routing_rule_id")
    private UUID routingRuleId;

    @Column(name = "status")
    private String status;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    public void prePersist()
    {
        this.receivedAt = LocalDateTime.now();
        this.status = "received";
    }
}