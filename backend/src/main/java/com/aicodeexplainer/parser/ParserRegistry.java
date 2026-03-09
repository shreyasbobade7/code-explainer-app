package com.aicodeexplainer.parser;

import com.aicodeexplainer.model.DetectedElements;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registry that delegates to the appropriate parser based on language.
 */
@Component
public class ParserRegistry {

    private final List<CodeElementParser> parsers;

    public ParserRegistry(List<CodeElementParser> parsers) {
        this.parsers = parsers;
    }

    public DetectedElements parse(String code, String language) {
        return parsers.stream()
                .filter(p -> p.supports(language))
                .findFirst()
                .map(p -> p.parse(code))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported language: " + language));
    }
}
