package com.aicodeexplainer.controller;

import com.aicodeexplainer.model.ExplainRequest;
import com.aicodeexplainer.model.ExplainResponse;
import com.aicodeexplainer.service.CodeExplainerService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class CodeExplainerController {

    private final CodeExplainerService codeExplainerService;

    public CodeExplainerController(CodeExplainerService codeExplainerService) {
        this.codeExplainerService = codeExplainerService;
    }

    @PostMapping(value = "/explain", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ExplainResponse>> explain(@Valid @RequestBody ExplainRequest request) {
        return codeExplainerService.explain(request)
                .map(ResponseEntity::ok);
    }
}
