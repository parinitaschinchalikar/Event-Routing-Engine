package com.eventrouter.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PublishEventRequest
{
    private String eventType;
    private Map<String, Object> data;
}