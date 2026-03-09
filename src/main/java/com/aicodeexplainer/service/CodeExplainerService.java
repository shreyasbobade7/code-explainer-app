package com.aicodeexplainer.service;

import com.aicodeexplainer.model.DetectedElements;
import com.aicodeexplainer.model.ExplainRequest;
import com.aicodeexplainer.model.ExplainResponse;
import com.aicodeexplainer.parser.ParserRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class CodeExplainerService {

    private static final Logger log = LoggerFactory.getLogger(CodeExplainerService.class);

    private final ParserRegistry parserRegistry;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public CodeExplainerService(ParserRegistry parserRegistry,
                                GeminiService geminiService,
                                ObjectMapper objectMapper) {
        this.parserRegistry = parserRegistry;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
    }

    public Mono<ExplainResponse> explain(ExplainRequest request) {

        String code = request.getCode().trim();
        String language = request.getLanguage().toLowerCase();

        DetectedElements detectedElements = parserRegistry.parse(code, language);

        String detectedElementsJson;

        try {
            detectedElementsJson = objectMapper.writeValueAsString(detectedElements);
        } catch (Exception e) {
            detectedElementsJson = detectedElements.toString();
        }

        String annotatedCode = annotateCodeWithElements(code, detectedElements);

        return geminiService.explainCode(annotatedCode, language, detectedElementsJson)
                .map(llmResponse -> parseLlmResponse(llmResponse, detectedElements))
                .onErrorResume(e -> {
                    log.error("Error explaining code", e);
                    return Mono.error(e);
                });
    }

    private String annotateCodeWithElements(String code, DetectedElements elements) {

        StringBuilder sb = new StringBuilder(code);

        sb.append("\n\n[Detected: functions=")
                .append(elements.getFunctions())
                .append(", loops=")
                .append(elements.getLoops())
                .append(", variables=")
                .append(elements.getVariables())
                .append("]");

        return sb.toString();
    }

    private ExplainResponse parseLlmResponse(String llmResponse, DetectedElements detectedElements) {

        String cleaned = llmResponse.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        cleaned = cleaned.trim();

        try {

            JsonNode root = objectMapper.readTree(cleaned);

            String explanation = getText(root, "explanation").orElse("");
            String optimizedCode = getText(root, "optimizedCode").orElse("");
            String timeComplexity = getText(root, "timeComplexity").orElse("Unknown");

            return ExplainResponse.builder()
                    .explanation(explanation)
                    .optimizedCode(optimizedCode)
                    .timeComplexity(timeComplexity)
                    .detectedElements(detectedElements)
                    .build();

        } catch (Exception e) {

            log.warn("Could not parse LLM response as JSON, using raw response", e);

            return ExplainResponse.builder()
                    .explanation(cleaned)
                    .optimizedCode("")
                    .timeComplexity("Unknown")
                    .detectedElements(detectedElements)
                    .build();
        }
    }

    private Optional<String> getText(JsonNode node, String field) {

        JsonNode child = node.get(field);

        return child != null && child.isTextual()
                ? Optional.of(child.asText())
                : Optional.empty();
    }
}