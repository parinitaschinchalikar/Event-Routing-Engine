package com.eventrouter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrichmentResult
{
    private BigDecimal riskScore;
    private Boolean anomalyFlag;
    private String recommendedAction;
    private String reasoning;
}