package com.aicodeexplainer.parser;

import com.aicodeexplainer.model.DetectedElements;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Python code using AST via lightweight subprocess script - extracts
 * functions, loops, conditionals, and variable assignments.
 */
@Component
public class PythonParser implements CodeElementParser {

    private static final Logger log = LoggerFactory.getLogger(PythonParser.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Pattern FUNCTION_DEF = Pattern.compile("\\bdef\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    private static final Pattern LAMBDA = Pattern.compile("\\blambda\\b");
    private static final Pattern FOR_LOOP = Pattern.compile("\\bfor\\s+");
    private static final Pattern WHILE_LOOP = Pattern.compile("\\bwhile\\s+");
    private static final Pattern IF_COND = Pattern.compile("\\bif\\s+");
    private static final Pattern TERNARY = Pattern.compile("\\bif\\b.*\\belse\\b");
    private static final Pattern VARIABLE_DECL = Pattern.compile(
            "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(?!def\\b|class\\b|lambda\\b)"
    );

    private static final Set<String> PYTHON_RESERVED = Set.of(
            "def", "class", "lambda", "if", "else", "elif", "for", "while",
            "return", "in", "and", "or", "not", "True", "False", "None",
            "import", "from", "with", "try", "except", "finally", "raise"
    );

    @Value("${app.python.path:python}")
    private String pythonPath;

    @Value("${app.python.ast.timeout-seconds:10}")
    private int timeoutSeconds;

    @Override
    public DetectedElements parse(String code) {
        DetectedElements result = runSubprocess(code);
        if (result != null) {
            return result;
        }
        log.warn("Python subprocess parse failed, falling back to regex");
        return fallbackParse(code);
    }

    private DetectedElements runSubprocess(String code) {
        Path scriptPath = null;
        try {
            var resource = new ClassPathResource("scripts/parse_python.py");
            scriptPath = Files.createTempFile("parse_python_", ".py");
            try (var in = resource.getInputStream()) {
                Files.write(scriptPath, in.readAllBytes());
            }

            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath.toAbsolutePath().toString());
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            try (var out = new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8)) {
                out.write(code);
                out.flush();
            }

            StringBuilder stdout = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line);
                }
            }

            boolean finished = proc.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                proc.destroyForcibly();
                return null;
            }
            if (proc.exitValue() != 0) {
                log.warn("Python script exited with {}: {}", proc.exitValue(), stdout);
                return null;
            }

            JsonNode root = OBJECT_MAPPER.readTree(stdout.toString());
            List<String> functions = arrayToList(root.get("functions"));
            List<String> loops = arrayToList(root.get("loops"));
            List<String> conditionals = arrayToList(root.get("conditionals"));
            List<String> variables = arrayToList(root.get("variables"));

            return DetectedElements.builder()
                    .functions(functions)
                    .loops(loops)
                    .conditionals(conditionals)
                    .variables(variables)
                    .build();
        } catch (Exception e) {
            log.debug("Subprocess parse failed: {}", e.getMessage());
            return null;
        } finally {
            if (scriptPath != null) {
                try {
                    Files.deleteIfExists(scriptPath);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private List<String> arrayToList(JsonNode node) {
        if (node == null || !node.isArray()) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        node.forEach(e -> list.add(e.asText()));
        return list;
    }

    private DetectedElements fallbackParse(String code) {
        Set<String> functions = new LinkedHashSet<>();
        Set<String> loops = new LinkedHashSet<>();
        Set<String> conditionals = new LinkedHashSet<>();
        Set<String> variables = new LinkedHashSet<>();

        Matcher m = FUNCTION_DEF.matcher(code);
        while (m.find()) functions.add(m.group(1));
        if (LAMBDA.matcher(code).find()) functions.add("lambda");
        if (FOR_LOOP.matcher(code).find()) loops.add("for");
        if (WHILE_LOOP.matcher(code).find()) loops.add("while");
        if (IF_COND.matcher(code).find()) conditionals.add("if");
        if (TERNARY.matcher(code).find()) conditionals.add("ternary");
        m = VARIABLE_DECL.matcher(code);
        while (m.find()) {
            String name = m.group(1);
            if (!PYTHON_RESERVED.contains(name)) variables.add(name);
        }

        return DetectedElements.builder()
                .functions(new ArrayList<>(functions))
                .loops(new ArrayList<>(loops))
                .conditionals(new ArrayList<>(conditionals))
                .variables(new ArrayList<>(variables))
                .build();
    }

    @Override
    public boolean supports(String language) {
        return "python".equalsIgnoreCase(language) || "py".equalsIgnoreCase(language);
    }
}
