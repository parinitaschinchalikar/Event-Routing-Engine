package com.eventrouter.handler;

import com.eventrouter.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseHandler implements EventHandler {

    @Override
    public void handle(Event event) {
        log.info("DATABASE HANDLER triggered for event:" +" id={} type={} riskScore={}",
                event.getId(),
                event.getEventType(),
                event.getRiskScore());

        // Event is already saved in DB by consumer
        // In production: trigger downstream DB workflows,
        // data warehouse sync, analytics pipeline etc.
        log.info("Event stored and processed: id={}",
                event.getId());
    }

    @Override
    public String getHandlerName() {
        return "database_handler";
    }
}