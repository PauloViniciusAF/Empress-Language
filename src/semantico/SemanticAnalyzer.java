package semantico;

import sintatico.TreeNode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticAnalyzer {
    private static class Symbol {
        enum Kind {
            VARIABLE, FUNCTION
        }

        final String name;
        final String type;
        final Kind kind;
        final int arity;

        Symbol(String name, String type, Kind kind, int arity) {
            this.name = name;
            this.type = type;
            this.kind = kind;
            this.arity = arity;
        }
    }

    private final Deque<Map<String, Symbol>> scopeStack = new ArrayDeque<>();
    private int scopeLevel = 0;

    private void enterScope() {
        scopeStack.push(new HashMap<>());
        scopeLevel++;
    }

    private void exitScope() {
        if (!scopeStack.isEmpty()) {
            scopeStack.pop();
            scopeLevel--;
        }
    }

    private void declare(Symbol s, int line) {
        Map<String, Symbol> current = scopeStack.peek();
        if (current == null)
            return;
        if (current.containsKey(s.name)) {
            Symbol existing = current.get(s.name);
            reportError(
                    String.format("Erro semântico: '%s' já foi declarado neste escopo (como %s).",
                            s.name, existing.kind == Symbol.Kind.FUNCTION ? "função" : "variável"),
                    line);
            return;
        }
        current.put(s.name, s);
    }

    private Symbol lookupSilent(String name) {
        for (Map<String, Symbol> scope : scopeStack)
            if (scope.containsKey(name))
                return scope.get(name);
        return null;
    }

    private Symbol lookup(String name, int line) {
        Symbol s = lookupSilent(name);
        if (s == null)
            reportError(String.format("Erro semântico: símbolo '%s' não foi declarado.", name),
                    line);
        return s;
    }

    private final StringBuilder errors = new StringBuilder();
    private int errorCount = 0;

    private void reportError(String msg, int line) {
        errorCount++;
        errors.append("  [").append(errorCount).append("] ").append(msg);
        if (line > 0)
            errors.append(" (linha ").append(line).append(")");
        errors.append("\n");
    }

    private void reportError(String msg) {
        reportError(msg, 0);
    }

    public void analyze(TreeNode root) throws SemanticException {
        scopeStack.push(new HashMap<>());
        visitNode(root);
        scopeStack.pop();
        if (errorCount > 0)
            throw new SemanticException(
                    errorCount + " erro(s) semântico(s) encontrado(s):\n" + errors);
    }

    private void visitNode(TreeNode node) {
        if (node == null)
            return;
        String v = node.getValue();
        if (v == null)
            return;
        switch (v) {
            case "file":
            case "bloco":
                visitChildren(node);
                break;
            case "cmdID":
                visitCmdID(node);
                break;
            case "cmdDefFunc":
                visitDefFunc(node);
                break;
            case "cmdIf":
            case "cmdWhile":
                enterScope();
                visitChildrenCheckingIds(node);
                exitScope();
                break;
            case "cmdReturn":
                visitChildrenCheckingIds(node);
                break;
            case "cmdFor":
                enterScope();
                declareForLoopVariables(node);
                visitChildrenCheckingIds(node);
                exitScope();
                break;
            case "cmdPrint":
            case "cmdInput":
                visitChildrenCheckingIds(node);
                break;
            default:
                visitChildren(node);
                break;
        }
    }

    private void visitChildren(TreeNode node) {
        for (TreeNode child : node.getChildren())
            visitNode(child);
    }

    private void visitChildrenCheckingIds(TreeNode node) {
        for (TreeNode child : node.getChildren()) {
            String v = child.getValue();
            if (v == null)
                continue;
            if (isRuleNode(v)) {
                visitNode(child);
                continue;
            }
            if (isIdentifier(v) && !isPunctuation(v) && !isCKeyword(v) && !isTypeLiteral(v)
                    && classifyLiteral(v).equals("unknown")) {
                lookup(v, child.getLine());
            }
            if (!child.getChildren().isEmpty())
                visitChildrenCheckingIds(child);
        }
    }

    private void visitCmdID(TreeNode node) {
        List<TreeNode> ch = node.getChildren();
        if (ch.isEmpty())
            return;
        String first = ch.get(0).getValue();
        if (isTypeLiteral(first)) {
            if (ch.size() < 2)
                return;
            String type = normalizeType(first);
            String varName = ch.get(1).getValue();
            declare(new Symbol(varName, type, Symbol.Kind.VARIABLE, 0), ch.get(1).getLine());
            if (ch.size() > 3) {
                checkIdsInRange(ch, 3, ch.size() - 1);
                String rhsType = inferType(ch, 3, ch.size() - 1);
                if (!rhsType.equals("unknown") && !typesCompatible(type, rhsType)) {
                    reportError(String.format(
                            "Erro semântico: não é possível atribuir valor do tipo '%s' à variável '%s' declarada como '%s'.",
                            rhsType, varName, type), ch.get(3).getLine());
                }
            }
            return;
        }

        String name = first;
        Symbol sym = lookup(name, ch.get(0).getLine());
        if (sym == null)
            return;
        boolean isCall = ch.size() > 1 && "args".equals(ch.get(1).getValue());

        if (isCall) {
            if (sym.kind != Symbol.Kind.FUNCTION) {
                reportError(String.format(
                        "Erro semântico: '%s' é uma variável e não pode ser chamada como função.",
                        name), ch.get(0).getLine());
                return;
            }
            TreeNode argsNode = ch.get(1);
            int provided = argsNode.getChildren().size();
            if (provided != sym.arity) {
                reportError(String.format(
                        "Erro semântico: função '%s' espera %d argumento(s), mas recebeu %d.", name,
                        sym.arity, provided), ch.get(0).getLine());
            }
            checkIdsInRange(argsNode.getChildren(), 0, argsNode.getChildren().size());
            return;
        }

        if (sym.kind == Symbol.Kind.FUNCTION) {
            reportError(String
                    .format("Erro semântico: '%s' é uma função; use '(' ')' para chamá-la.", name),
                    ch.get(0).getLine());
            return;
        }
        if (ch.size() > 2) {
            checkIdsInRange(ch, 2, ch.size() - 1);
            String rhsType = inferType(ch, 2, ch.size() - 1);
            if (!rhsType.equals("unknown") && !typesCompatible(sym.type, rhsType)) {
                reportError(String.format(
                        "Erro semântico: não é possível atribuir valor do tipo '%s' à variável '%s' do tipo '%s'.",
                        rhsType, name, sym.type), ch.get(2).getLine());
            }
        }
    }

    private void visitDefFunc(TreeNode node) {
        List<TreeNode> ch = node.getChildren();
        if (ch.size() < 2)
            return;

        // ch.get(0) = tipo de retorno ("int", "float", "bool", "char*", "void")
        // ch.get(1) = nome da função
        String returnTypeRaw = ch.get(0).getValue();
        String returnType = normalizeReturnType(returnTypeRaw);
        String funcName = ch.get(1).getValue();

        // Acha onde começa o bloco
        int blocoIdx = -1;
        for (int i = 0; i < ch.size(); i++) {
            if ("bloco".equals(ch.get(i).getValue())) {
                blocoIdx = i;
                break;
            }
        }

        // Parâmetros ficam entre índice 2 e blocoIdx (exclusive)
        int arity = 0;
        for (int i = 2; blocoIdx > 2 && i < blocoIdx; i++) {
            String v = ch.get(i).getValue();
            if (!",".equals(v) && !isTypeLiteral(v))
                arity++;
        }

        declare(new Symbol(funcName, returnType, Symbol.Kind.FUNCTION, arity), ch.get(1).getLine());
        enterScope();

        // Declara os parâmetros no escopo da função
        String pendingType = null;
        for (int i = 2; blocoIdx > 2 && i < blocoIdx; i++) {
            TreeNode child = ch.get(i);
            String v = child.getValue();
            if (",".equals(v)) {
                pendingType = null;
                continue;
            }
            if (isTypeLiteral(v)) {
                pendingType = normalizeType(v);
                continue;
            }
            String paramType = (pendingType != null) ? pendingType : "unknown";
            declare(new Symbol(v, paramType, Symbol.Kind.VARIABLE, 0), child.getLine());
            pendingType = null;
        }

        if (blocoIdx >= 0)
            visitNode(ch.get(blocoIdx));
        exitScope();
    }

    private void checkIdsInRange(List<TreeNode> children, int from, int to) {
        for (int i = from; i < to && i < children.size(); i++) {
            TreeNode child = children.get(i);
            String v = child.getValue();
            if (v == null)
                continue;
            if (isIdentifier(v) && !isPunctuation(v) && !isCKeyword(v) && !isTypeLiteral(v)
                    && classifyLiteral(v).equals("unknown"))
                lookup(v, child.getLine());
            if (!child.getChildren().isEmpty())
                checkIdsInRange(child.getChildren(), 0, child.getChildren().size());
        }
    }

    private boolean isRuleNode(String v) {
        return switch (v) {
            case "file", "bloco", "cmdID", "cmdDefFunc", "cmdIf", "cmdWhile", "cmdFor", "cmdReturn", "cmdPrint", "cmdInput" -> true;
            default -> false;
        };
    }

    private boolean isTypeLiteral(String v) {
        return "int".equals(v) || "float".equals(v) || "bool".equals(v) || "char*".equals(v);
    }

    private String normalizeReturnType(String v) {
        if (v == null) return "void";
        return switch (v) {
            case "int"   -> "int";
            case "float" -> "float";
            case "bool"  -> "bool";
            case "char*" -> "string";
            case "void"  -> "void";
            default      -> "void";
        };
    }

    private String normalizeType(String v) {
        return switch (v) {
            case "int" -> "int";
            case "float" -> "float";
            case "bool" -> "bool";
            case "char*" -> "string";
            default -> "unknown";
        };
    }

    private String inferType(List<TreeNode> children, int from, int to) {
        String result = "unknown";
        for (int i = from; i < to && i < children.size(); i++) {
            String v = children.get(i).getValue();
            if (v == null)
                continue;
            String t = classifyLiteral(v);
            if (!t.equals("unknown")) {
                if (result.equals("unknown") || (result.equals("int") && t.equals("float")))
                    result = t;
                continue;
            }
            if (isIdentifier(v)) {
                Symbol sym = lookupSilent(v);
                if (sym != null && !sym.type.equals("unknown") && (result.equals("unknown")
                        || (result.equals("int") && sym.type.equals("float"))))
                    result = sym.type;
            }
        }
        return result;
    }

    private String classifyLiteral(String v) {
        if (v.matches("-?[0-9]+"))
            return "int";
        if (v.matches("-?[0-9]+\\.[0-9]+"))
            return "float";
        if ("true".equals(v) || "false".equals(v) || "истинный".equals(v) || "ложь".equals(v))
            return "bool";
        if (v.startsWith("\"") && v.endsWith("\""))
            return "string";
        return "unknown";
    }

    private boolean isIdentifier(String v) {
        return v != null && v.matches("[a-zA-Z_\\u0400-\\u04FF][a-zA-Z_0-9\\u0400-\\u04FF]*");
    }

    private boolean isPunctuation(String v) {
        return switch (v) {
            case "(", ")", "{", "}", "[", "]", ",", ";", "=", "+=", "-=", "*=", "/=", "%=", "^=", "+", "-", "*", "/", "%", "^", "++", "--", "==", "!=", "<", ">", "<=", ">=", "&&", "||" -> true;
            default -> false;
        };
    }

    private boolean isCKeyword(String v) {
        return switch (v) {
            case "if", "else", "while", "for", "return", "void", "printf", "scanf", "continue", "break", "true", "false", "int", "float", "bool", "char*" -> true;
            default -> false;
        };
    }

    private boolean typesCompatible(String declared, String assigned) {
        if ("unknown".equals(assigned))
            return true;
        if (declared.equals(assigned))
            return true;
        return "float".equals(declared) && "int".equals(assigned);
    }

    private void declareForLoopVariables(TreeNode node) {
        List<TreeNode> ch = node.getChildren();
        boolean inInit = true;
        for (int i = 0; i < ch.size() && inInit; i++) {
            TreeNode child = ch.get(i);
            String v = child.getValue();
            if (v == null)
                continue;
            if (isTypeLiteral(v) && i + 1 < ch.size()) {
                String type = normalizeType(v);
                String nextVal = ch.get(i + 1).getValue();
                if (nextVal != null && isIdentifier(nextVal) && !isPunctuation(nextVal)
                        && !isCKeyword(nextVal))
                    declare(new Symbol(nextVal, type, Symbol.Kind.VARIABLE, 0),
                            ch.get(i + 1).getLine());
            }
            if (";".equals(v))
                inInit = false;
        }
    }
}