package com.aicodeexplainer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DetectedElements {

    @Builder.Default
    private List<String> functions = new ArrayList<>();

    @Builder.Default
    private List<String> loops = new ArrayList<>();

    @Builder.Default
    private List<String> conditionals = new ArrayList<>();

    @Builder.Default
    private List<String> variables = new ArrayList<>();
}
