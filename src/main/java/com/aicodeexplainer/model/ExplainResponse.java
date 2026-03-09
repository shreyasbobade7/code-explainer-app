package com.aicodeexplainer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExplainResponse {

    private String explanation;
    private String optimizedCode;
    private String timeComplexity;
    private DetectedElements detectedElements;
}
