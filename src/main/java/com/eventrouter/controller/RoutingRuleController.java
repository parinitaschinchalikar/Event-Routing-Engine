package com.eventrouter.controller;

import com.eventrouter.model.RoutingRule;
import com.eventrouter.repository.RoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/routing-rules")
@RequiredArgsConstructor
public class RoutingRuleController {

    private final RoutingRuleRepository ruleRepository;

    // Get all active routing rules
    @GetMapping
    public ResponseEntity<List<RoutingRule>> getAllRules() {
        return ResponseEntity.ok(
                ruleRepository
                        .findByActiveTrueOrderByPriorityAsc());
    }

    // Get rules for a specific event type
    @GetMapping("/event-type/{eventType}")
    public ResponseEntity<List<RoutingRule>>
    getRulesByEventType(
            @PathVariable String eventType) {
        return ResponseEntity.ok(
                ruleRepository
                        .findByEventTypeAndActiveTrueOrderByPriorityAsc(
                                eventType));
    }

    // Create a new routing rule
    @PostMapping
    public ResponseEntity<RoutingRule> createRule(
            @RequestBody RoutingRule rule) {
        rule.setActive(true);
        return ResponseEntity.ok(
                ruleRepository.save(rule));
    }

    // Update a routing rule
    @PutMapping("/{id}")
    public ResponseEntity<RoutingRule> updateRule(
            @PathVariable UUID id,
            @RequestBody RoutingRule updated) {
        return ruleRepository.findById(id)
                .map(rule -> {
                    rule.setEventType(
                            updated.getEventType());
                    rule.setConditionField(
                            updated.getConditionField());
                    rule.setConditionOperator(
                            updated.getConditionOperator());
                    rule.setConditionValue(
                            updated.getConditionValue());
                    rule.setHandler(updated.getHandler());
                    rule.setPriority(updated.getPriority());
                    return ResponseEntity.ok(
                            ruleRepository.save(rule));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Deactivate a rule (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateRule(
            @PathVariable UUID id) {
        return ruleRepository.findById(id)
                .map(rule -> {
                    rule.setActive(false);
                    ruleRepository.save(rule);
                    return ResponseEntity.ok()
                            .<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}