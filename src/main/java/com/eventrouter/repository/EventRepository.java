package com.eventrouter.repository;

import com.eventrouter.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EventRepository
        extends JpaRepository<Event, UUID>
{
    List<Event> findByEventTypeOrderByReceivedAtDesc(
            String eventType);
    List<Event> findByRoutedToOrderByReceivedAtDesc(
            String routedTo);
    List<Event> findByStatusOrderByReceivedAtDesc(
            String status);
    long countByRoutedTo(String routedTo);
    long countByAnomalyFlagTrue();
}