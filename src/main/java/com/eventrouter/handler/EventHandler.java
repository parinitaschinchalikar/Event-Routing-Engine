package com.eventrouter.handler;

import com.eventrouter.model.Event;

public interface EventHandler
{
    void handle(Event event);
    String getHandlerName();
}