package com.eventrouter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventPayload
{
    private String eventType;
    private String sourceSystem;
    private Map<String, Object> data;
    private long timestamp;
}