package com.eventrouter.handler;

import com.eventrouter.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebhookHandler implements EventHandler {

    @Override
    public void handle(Event event) {
        log.info("WEBHOOK HANDLER triggered for event:" + " id={} type={}",
                event.getId(),
                event.getEventType());

        // In production: POST event payload to
        // configured webhook URL
        log.info("Webhook payload dispatched for: id={}",
                event.getId());
    }

    @Override
    public String getHandlerName() {
        return "webhook_handler";
    }
}