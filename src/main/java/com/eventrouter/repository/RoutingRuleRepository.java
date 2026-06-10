package com.eventrouter.repository;

import com.eventrouter.model.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RoutingRuleRepository
        extends JpaRepository<RoutingRule, UUID>
{
    List<RoutingRule> findByEventTypeAndActiveTrueOrderByPriorityAsc(
            String eventType);
    List<RoutingRule> findByActiveTrueOrderByPriorityAsc();
}