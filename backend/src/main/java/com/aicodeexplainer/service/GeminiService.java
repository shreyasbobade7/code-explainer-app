package com.aicodeexplainer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${app.gemini.api-key}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-2.0-flash}")
    private String modelName;

    public GeminiService(WebClient geminiWebClient, ObjectMapper objectMapper) {
        this.geminiWebClient = geminiWebClient;
        this.objectMapper = objectMapper;
    }

    public Mono<String> explainCode(String annotatedCode, String language, String detectedElementsJson) {
        String prompt = buildPrompt(annotatedCode, language, detectedElementsJson);

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{ Map.of("text", prompt) })
                }
        );

        // Build path: /models/{model}:generateContent
        String pathTemplate = "/models/gemini-2.0-flash:generateContent";

        return geminiWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(pathTemplate)
                        .queryParam("key", apiKey)
                        .build(modelName))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractGeminiText)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getRawStatusCode() == 404) {
                        log.error("Gemini model not found: {} - check app.gemini.model and permissions. Response body: {}",
                                modelName, ex.getResponseBodyAsString());
                        return Mono.error(new RuntimeException("Gemini model not found: " + modelName +
                                ". Run GET /v1beta/models?key=... to list available models."));
                    }
                    log.error("Gemini API error (status {}): {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
                    return Mono.error(new RuntimeException("Gemini API error: " + ex.getMessage()));
                });
    }

    private String extractGeminiText(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidate = root.path("candidates");
            if (!candidate.isArray() || candidate.size() == 0) {
                throw new RuntimeException("No candidates in Gemini response");
            }
            return candidate.get(0).path("content").path("parts").get(0).path("text").asText("");
        } catch (Exception e) {
            log.error("Failed to parse Gemini response", e);
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }

    private String buildPrompt(String code, String language, String astJson) {
        return """
                You are an expert code explainer. Return STRICT JSON only with fields:
                { "explanation":"...", "optimizedCode":"...", "timeComplexity":"..." }

                Language: %s

                AST metadata:
                %s

                Code:
                %s
                """.formatted(language, astJson, code);
    }
}