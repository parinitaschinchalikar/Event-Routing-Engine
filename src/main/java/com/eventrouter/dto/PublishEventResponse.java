package com.eventrouter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublishEventResponse
{
    private String eventId;
    private String eventType;
    private String status;
    private String message;
    private long timestamp;
}