package com.aicodeexplainer.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplainRequest {

    @NotBlank(message = "Code snippet is required")
    private String code;

    @NotBlank(message = "Language is required")
    @Pattern(regexp = "javascript|python", message = "Language must be 'javascript' or 'python'")
    private String language;
}
