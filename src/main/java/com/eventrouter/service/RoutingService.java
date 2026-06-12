package com.eventrouter.service;

import com.eventrouter.handler.EventHandler;
import com.eventrouter.model.Event;
import com.eventrouter.model.RoutingRule;
import com.eventrouter.repository.EventRepository;
import com.eventrouter.repository.RoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingService {

    private final RoutingRuleRepository ruleRepository;
    private final EventRepository eventRepository;
    private final List<EventHandler> handlers;

    // Build handler map from injected list
    private Map<String, EventHandler> getHandlerMap() {
        return handlers.stream()
                .collect(Collectors.toMap(
                        EventHandler::getHandlerName,
                        Function.identity()
                ));
    }

    public void routeEvent(Event event) {
        log.info("Routing event: id={} type={} " +
                        "riskScore={} anomaly={}",
                event.getId(),
                event.getEventType(),
                event.getRiskScore(),
                event.getAnomalyFlag());

        // Get rules for this event type ordered by priority
        List<RoutingRule> rules = ruleRepository
                .findByEventTypeAndActiveTrueOrderByPriorityAsc(
                        event.getEventType());

        if (rules.isEmpty()) {
            log.warn("No routing rules found for " +
                            "event type: {} — using default",
                    event.getEventType());
            executeHandler(event, "database_handler", null);
            return;
        }

        // Evaluate rules in priority order
        for (RoutingRule rule : rules) {
            if (evaluateRule(rule, event)) {
                log.info("Rule matched: id={} " +
                                "condition={} {} {} handler={}",
                        rule.getId(),
                        rule.getConditionField(),
                        rule.getConditionOperator(),
                        rule.getConditionValue(),
                        rule.getHandler());

                executeHandler(event,
                        rule.getHandler(), rule);
                return;
            }
        }

        // No rule matched — use default
        log.warn("No rule matched for event: {} " +
                "— using default handler", event.getId());
        executeHandler(event, "database_handler", null);
    }

    private boolean evaluateRule(RoutingRule rule,
                                 Event event) {
        try {
            String field = rule.getConditionField();
            String operator = rule.getConditionOperator();
            String value = rule.getConditionValue();

            return switch (field) {
                case "risk_score" -> evaluateNumeric(
                        event.getRiskScore(),
                        operator,
                        new BigDecimal(value));
                case "anomaly_flag" -> evaluateBoolean(
                        event.getAnomalyFlag(),
                        operator,
                        Boolean.parseBoolean(value));
                default -> {
                    log.warn("Unknown condition field: {}",
                            field);
                    yield false;
                }
            };
        } catch (Exception e) {
            log.error("Rule evaluation failed: {}",
                    e.getMessage());
            return false;
        }
    }

    private boolean evaluateNumeric(BigDecimal actual,
                                    String operator,
                                    BigDecimal threshold) {
        if (actual == null) return false;
        return switch (operator) {
            case "gt"  -> actual.compareTo(threshold) > 0;
            case "gte" -> actual.compareTo(threshold) >= 0;
            case "lt"  -> actual.compareTo(threshold) < 0;
            case "lte" -> actual.compareTo(threshold) <= 0;
            case "eq"  -> actual.compareTo(threshold) == 0;
            default -> false;
        };
    }

    private boolean evaluateBoolean(Boolean actual,
                                    String operator,
                                    Boolean expected) {
        if (actual == null) return false;
        return switch (operator) {
            case "eq"  -> actual.equals(expected);
            case "neq" -> !actual.equals(expected);
            default -> false;
        };
    }

    private void executeHandler(Event event,
                                String handlerName,
                                RoutingRule rule) {
        Map<String, EventHandler> handlerMap =
                getHandlerMap();
        EventHandler handler = handlerMap.get(handlerName);

        if (handler == null) {
            log.error("Handler not found: {}",
                    handlerName);
            return;
        }

        // Execute handler
        handler.handle(event);

        // Update event with routing decision
        event.setRoutedTo(handlerName);
        if (rule != null) {
            event.setRoutingRuleId(rule.getId());
        }
        event.setStatus("routed");
        event.setProcessedAt(LocalDateTime.now());
        eventRepository.save(event);

        log.info("Event routed: id={} handler={}",
                event.getId(), handlerName);
    }
}