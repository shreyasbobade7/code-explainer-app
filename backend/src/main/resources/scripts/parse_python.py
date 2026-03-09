#!/usr/bin/env python3
"""
Lightweight Python AST parser - extracts functions, loops, conditionals, and variables.
Output: JSON to stdout. Input: code via stdin.
"""

import ast
import json
import sys


def extract_elements(code: str) -> dict:
    functions = []
    loops = []
    conditionals = []
    variables = []

    try:
        tree = ast.parse(code)
    except SyntaxError as e:
        return {
            "functions": [],
            "loops": [],
            "conditionals": [],
            "variables": [],
            "error": str(e),
        }

    reserved = frozenset({
        "def", "class", "lambda", "if", "else", "elif", "for", "while",
        "return", "in", "and", "or", "not", "True", "False", "None",
        "import", "from", "with", "try", "except", "finally", "raise",
        "assert", "yield", "async", "await", "pass", "break", "continue",
        "global", "nonlocal", "del", "as", "match", "case",
    })

    class Visitor(ast.NodeVisitor):
        def visit_FunctionDef(self, node):
            functions.append(node.name)
            self.generic_visit(node)

        def visit_AsyncFunctionDef(self, node):
            functions.append(node.name)
            self.generic_visit(node)

        def visit_Lambda(self, node):
            functions.append("lambda")
            self.generic_visit(node)

        def visit_For(self, node):
            loops.append("for")
            self.generic_visit(node)

        def visit_AsyncFor(self, node):
            loops.append("for")
            self.generic_visit(node)

        def visit_While(self, node):
            loops.append("while")
            self.generic_visit(node)

        def visit_If(self, node):
            conditionals.append("if")
            self.generic_visit(node)

        def visit_IfExp(self, node):
            conditionals.append("ternary")
            self.generic_visit(node)

        def visit_Match(self, node):
            conditionals.append("match")
            self.generic_visit(node)

        def visit_Assign(self, node):
            for target in node.targets:
                self._collect_assign_target(target)
            self.generic_visit(node)

        def visit_AnnAssign(self, node):
            if node.target:
                self._collect_assign_target(node.target)
            self.generic_visit(node)

        def visit_AugAssign(self, node):
            self._collect_assign_target(node.target)
            self.generic_visit(node)

        def _collect_assign_target(self, node):
            if isinstance(node, ast.Name):
                if node.id not in reserved:
                    variables.append(node.id)
            elif isinstance(node, ast.Tuple):
                for elt in node.elts:
                    self._collect_assign_target(elt)
            elif isinstance(node, ast.List):
                for elt in node.elts:
                    self._collect_assign_target(elt)
            elif isinstance(node, ast.Starred):
                self._collect_assign_target(node.value)
            elif isinstance(node, (ast.Subscript, ast.Attribute)):
                pass  # not a new variable assignment

    Visitor().visit(tree)

    return {
        "functions": list(dict.fromkeys(functions)),
        "loops": list(dict.fromkeys(loops)),
        "conditionals": list(dict.fromkeys(conditionals)),
        "variables": list(dict.fromkeys(variables)),
    }


if __name__ == "__main__":
    code = sys.stdin.read()
    result = extract_elements(code)
    print(json.dumps(result))
