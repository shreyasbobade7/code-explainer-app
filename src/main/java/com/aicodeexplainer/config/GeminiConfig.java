package com.aicodeexplainer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiConfig {

    private static final String GEMINI_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta";

    @Value("${app.gemini.api-key}")
    private String apiKey;

    @Bean("geminiWebClient")
    public WebClient geminiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(GEMINI_BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public String geminiApiKey() {
        return apiKey;
    }
}