package com.aicodeexplainer.parser;

import com.aicodeexplainer.model.DetectedElements;

/**
 * Interface for parsing code and extracting key elements (functions, loops, variables).
 */
public interface CodeElementParser {

    DetectedElements parse(String code);

    boolean supports(String language);
}
