package com.eventrouter.repository;

import com.eventrouter.model.DeadLetterEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DeadLetterEventRepository
        extends JpaRepository<DeadLetterEvent, UUID> {
    long countByEventType(String eventType);
}