package com.aicodeexplainer.parser;

import com.aicodeexplainer.model.DetectedElements;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Parser for JavaScript/ECMAScript using Mozilla Rhino AST - extracts functions, loops,
 * conditionals, and variable assignments.
 */
@Component
public class JavaScriptParser implements CodeElementParser {

    private static final Logger log = LoggerFactory.getLogger(JavaScriptParser.class);

    @Override
    public DetectedElements parse(String code) {
        Set<String> functions = new LinkedHashSet<>();
        Set<String> loops = new LinkedHashSet<>();
        Set<String> conditionals = new LinkedHashSet<>();
        Set<String> variables = new LinkedHashSet<>();

        try {
            CompilerEnvirons env = new CompilerEnvirons();
            env.setRecordingLocalJsDocComments(false);
            env.setRecordingComments(false);
            Parser p = new Parser(env, null);
            AstRoot ast = p.parse(code, null, 0);

            ast.visit(new NodeVisitor() {
                @Override
                public boolean visit(AstNode node) {
                    if (node instanceof FunctionNode fn) {
                        String name = fn.getName();
                        if (name != null && !name.isBlank()) {
                            functions.add(name);
                        } else {
                            functions.add("(anonymous)");
                        }
                    } else if (node instanceof Loop) {
                        if (node instanceof ForLoop) loops.add("for");
                        else if (node instanceof WhileLoop) loops.add("while");
                        else if (node instanceof DoLoop) loops.add("do-while");
                    } else if (node instanceof IfStatement) {
                        conditionals.add("if");
                    } else if (node instanceof ConditionalExpression) {
                        conditionals.add("ternary");
                    } else if (node instanceof VariableDeclaration vd) {
                        for (VariableInitializer vi : vd.getVariables()) {
                            AstNode target = vi.getTarget();
                            if (target instanceof Name n) {
                                variables.add(n.getIdentifier());
                            } else if (target instanceof DestructuringForm) {
                                collectDestructuringVars((DestructuringForm) target, variables);
                            }
                        }
                    } else if (node instanceof Name n && isAssignmentTarget(node)) {
                        String id = n.getIdentifier();
                        if (id != null && !id.isBlank() && !isReserved(id)) {
                            variables.add(id);
                        }
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            log.warn("Rhino AST parse failed, falling back to regex: {}", e.getMessage());
            return fallbackParse(code);
        }

        return DetectedElements.builder()
                .functions(new ArrayList<>(functions))
                .loops(new ArrayList<>(loops))
                .conditionals(new ArrayList<>(conditionals))
                .variables(new ArrayList<>(variables))
                .build();
    }

    private void collectDestructuringVars(DestructuringForm node, Set<String> variables) {
        if (node instanceof ArrayLiteral al) {
            for (AstNode elem : al.getElements()) {
                if (elem instanceof Name n) variables.add(n.getIdentifier());
                else if (elem instanceof DestructuringForm df) collectDestructuringVars(df, variables);
            }
        } else if (node instanceof ObjectLiteral ol) {
            for (ObjectProperty prop : ol.getElements()) {
                AstNode val = prop.getRight();
                if (val instanceof Name n) variables.add(n.getIdentifier());
                else if (val instanceof DestructuringForm df) collectDestructuringVars(df, variables);
            }
        }
    }

    private boolean isAssignmentTarget(AstNode node) {
        AstNode parent = node.getParent();
        if (parent instanceof VariableInitializer vi) return vi.getTarget() == node;
        if (parent instanceof Assignment) return true;
        return false;
    }

    private boolean isReserved(String id) {
        return Set.of("undefined", "null", "true", "false", "arguments", "this").contains(id);
    }

    private DetectedElements fallbackParse(String code) {
        Set<String> functions = new LinkedHashSet<>();
        Set<String> loops = new LinkedHashSet<>();
        Set<String> conditionals = new LinkedHashSet<>();
        Set<String> variables = new LinkedHashSet<>();

        var fnDecl = java.util.regex.Pattern.compile("\\bfunction\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\(");
        var fnArrow = java.util.regex.Pattern.compile("(?:const|let|var)\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*=\\s*(?:\\([^)]*\\)|[a-zA-Z_$][a-zA-Z0-9_$]*)\\s*=>");
        var varDecl = java.util.regex.Pattern.compile("\\b(const|let|var)\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)");
        var fnMatcher = fnDecl.matcher(code);
        while (fnMatcher.find()) functions.add(fnMatcher.group(1));
        fnMatcher = fnArrow.matcher(code);
        while (fnMatcher.find()) functions.add(fnMatcher.group(1));
        if (java.util.regex.Pattern.compile("\\bfunction\\s*\\(").matcher(code).find()) functions.add("(anonymous)");
        if (java.util.regex.Pattern.compile("\\bfor\\s*\\(").matcher(code).find()) loops.add("for");
        if (java.util.regex.Pattern.compile("\\bwhile\\s*\\(").matcher(code).find()) loops.add("while");
        if (java.util.regex.Pattern.compile("\\bif\\s*\\(").matcher(code).find()) conditionals.add("if");
        if (java.util.regex.Pattern.compile("\\?.*:").matcher(code).find()) conditionals.add("ternary");
        var varMatcher = varDecl.matcher(code);
        while (varMatcher.find()) variables.add(varMatcher.group(2));

        return DetectedElements.builder()
                .functions(new ArrayList<>(functions))
                .loops(new ArrayList<>(loops))
                .conditionals(new ArrayList<>(conditionals))
                .variables(new ArrayList<>(variables))
                .build();
    }

    @Override
    public boolean supports(String language) {
        return "javascript".equalsIgnoreCase(language) || "js".equalsIgnoreCase(language);
    }
}
