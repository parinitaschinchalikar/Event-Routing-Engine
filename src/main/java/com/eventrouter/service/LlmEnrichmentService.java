package com.eventrouter.service;

import com.eventrouter.dto.EnrichmentResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.OllamaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmEnrichmentService {

    private final ObjectMapper objectMapper;

    @Value("${ollama.base.url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    public EnrichmentResult enrichEvent(String eventType,
                                        String payload) {
        try {
            log.info("Enriching event: type={}", eventType);

            String prompt = buildPrompt(eventType, payload);

            OllamaAPI api = new OllamaAPI(ollamaBaseUrl);
            api.setRequestTimeoutSeconds(60);

            OllamaResult result = api.generate(
                    ollamaModel, prompt, false, new io.github.ollama4j.utils.OptionsBuilder().build());

            String response = result.getResponse();
            log.info("LLM enrichment response: {}", response);

            return parseEnrichmentResponse(response);

        } catch (Exception e) {
            log.error("LLM enrichment failed: {}",
                    e.getMessage());
            // Return default enrichment on failure
            return EnrichmentResult.builder()
                    .riskScore(new BigDecimal("0.5"))
                    .anomalyFlag(false)
                    .recommendedAction("database_handler")
                    .reasoning("Default enrichment - LLM unavailable")
                    .build();
        }
    }

    private String buildPrompt(String eventType,
                               String payload) {
        return """
                Analyze this business event and return ONLY a
                JSON object with no explanation or extra text.
                
                Event type: %s
                Event payload: %s
                
                Return exactly this JSON structure:
                {
                  "riskScore": <number between 0.0 and 1.0>,
                  "anomalyFlag": <true or false>,
                  "recommendedAction": <"alert_handler" or "database_handler">,
                  "reasoning": <brief explanation under 20 words>
                }
                
                Rules:
                - riskScore above 0.7 means high risk
                - anomalyFlag true if something unusual detected
                - Use alert_handler for high risk events
                - Use database_handler for normal events
                - Return ONLY the JSON, nothing else
                """.formatted(eventType, payload);
    }

    private EnrichmentResult parseEnrichmentResponse(
            String response) {
        try {
            // Clean response — remove markdown if present
            String cleaned = response
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // Find JSON object in response
            int start = cleaned.indexOf("{");
            int end = cleaned.lastIndexOf("}");

            if (start == -1 || end == -1) {
                throw new RuntimeException(
                        "No JSON found in response");
            }

            String json = cleaned.substring(start, end + 1);

            // Parse manually to avoid Jackson version issues
            double riskScore = extractDouble(json,
                    "riskScore");
            boolean anomalyFlag = extractBoolean(json,
                    "anomalyFlag");
            String recommendedAction = extractString(json,
                    "recommendedAction");
            String reasoning = extractString(json,
                    "reasoning");

            return EnrichmentResult.builder()
                    .riskScore(BigDecimal.valueOf(riskScore))
                    .anomalyFlag(anomalyFlag)
                    .recommendedAction(recommendedAction)
                    .reasoning(reasoning)
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse LLM response: {} " +
                    "using defaults", e.getMessage());
            return EnrichmentResult.builder()
                    .riskScore(new BigDecimal("0.5"))
                    .anomalyFlag(false)
                    .recommendedAction("database_handler")
                    .reasoning("Parse failed - using defaults")
                    .build();
        }
    }

    private double extractDouble(String json, String key) {
        try {
            String pattern = "\"" + key + "\"";
            int idx = json.indexOf(pattern);
            if (idx == -1) return 0.5;
            int colon = json.indexOf(":", idx);
            int comma = json.indexOf(",", colon);
            int brace = json.indexOf("}", colon);
            int end = Math.min(
                    comma == -1 ? brace : comma, brace);
            return Double.parseDouble(
                    json.substring(colon + 1, end).trim());
        } catch (Exception e) {
            return 0.5;
        }
    }

    private boolean extractBoolean(String json, String key) {
        try {
            String pattern = "\"" + key + "\"";
            int idx = json.indexOf(pattern);
            if (idx == -1) return false;
            int colon = json.indexOf(":", idx);
            int comma = json.indexOf(",", colon);
            int brace = json.indexOf("}", colon);
            int end = Math.min(
                    comma == -1 ? brace : comma, brace);
            return Boolean.parseBoolean(
                    json.substring(colon + 1, end).trim());
        } catch (Exception e) {
            return false;
        }
    }

    private String extractString(String json, String key) {
        try {
            String pattern = "\"" + key + "\"";
            int idx = json.indexOf(pattern);
            if (idx == -1) return "database_handler";
            int colon = json.indexOf(":", idx);
            int firstQuote = json.indexOf("\"", colon + 1);
            int lastQuote = json.indexOf("\"",
                    firstQuote + 1);
            return json.substring(firstQuote + 1, lastQuote);
        } catch (Exception e) {
            return "database_handler";
        }
    }
}