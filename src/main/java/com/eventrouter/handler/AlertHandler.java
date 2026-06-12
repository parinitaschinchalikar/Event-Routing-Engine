package com.eventrouter.handler;

import com.eventrouter.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlertHandler implements EventHandler {

    @Override
    public void handle(Event event) {
        log.warn("ALERT HANDLER triggered for event: " +"id={} type={} riskScore={} anomaly={}",
                event.getId(),
                event.getEventType(),
                event.getRiskScore(),
                event.getAnomalyFlag());

        log.warn("Recommended action: {} — " +"Payload: {}",
                event.getRecommendedAction(),
                event.getPayload());

        // In production: send email, PagerDuty,
        // Slack notification etc.
    }

    @Override
    public String getHandlerName() {
        return "alert_handler";
    }
}