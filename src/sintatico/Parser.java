package sintatico;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lexer.Token;

public class Parser {
    List<Token> tokens;
    Token token;
    private BufferedWriter writer;
    private String outFilePath;
    private String lastType = null;
    private String[] codeLines;
    private int currentLine = 1;
    private Tree ast;
    private boolean showTree;

    // Buffers para gerar o código separadamente
    private StringBuilder globalCode = new StringBuilder(); // funções
    private StringBuilder mainCode = new StringBuilder(); // dentro da main
    private boolean insideFunction = false;

    // Buffer temporário para capturar o argumento do printf
    private StringBuilder printfArgBuffer = null;
    // Buffers para gerar scanf corretamente em cmdInput
    private boolean scanInputMode = false;
    private StringBuilder scanfFormatBuffer = null;
    private StringBuilder scanfArgsBuffer = null;

    // Tabela de símbolos para tipos de variáveis
    private Map<String, String> symbolTable = new HashMap<>();

    public Parser(List<Token> tokens, String inputFilePath, String sourceCode) {
        this(tokens, inputFilePath, sourceCode, false, null);
    }

    public Parser(List<Token> tokens, String inputFilePath, String sourceCode, boolean showTree) {
        this(tokens, inputFilePath, sourceCode, showTree, null);
    }

    public Parser(List<Token> tokens, String inputFilePath, String sourceCode, String outputDir) {
        this(tokens, inputFilePath, sourceCode, false, outputDir);
    }

    public Parser(List<Token> tokens, String inputFilePath, String sourceCode, boolean showTree,
            String outputDir) {
        this.tokens = tokens;
        this.codeLines = sourceCode.split("\n");
        this.ast = new Tree();
        this.showTree = showTree;
        try {
            String outName = new File(inputFilePath).getName();
            if (outName.endsWith(".emp")) {
                outName = outName.substring(0, outName.length() - 4) + ".c";
            } else {
                outName = outName + ".c";
            }
            if (outputDir != null && !outputDir.isEmpty()) {
                File outDirFile = new File(outputDir);
                if (!outDirFile.exists())
                    outDirFile.mkdirs();
                this.outFilePath = outputDir + File.separator + outName;
            } else {
                this.outFilePath = outName;
            }
            File outFile = new File(outFilePath);
            this.writer = new BufferedWriter(new FileWriter(outFile));
        } catch (IOException e) {
            System.out.println("Erro ao criar arquivo de saída: " + e.getMessage());
            this.writer = null;
        }
    }

    // Escrita nos buffers apropriados
    private void write(String code) {
        if (printfArgBuffer != null) { // ← adiciona esse bloco
            printfArgBuffer.append(code);
            return;
        }
        if (insideFunction) {
            globalCode.append(code);
        } else {
            mainCode.append(code);
        }
    }

    // Escreve um operador respeitando o printfArgBuffer quando ativo
    private void writeOp(String op) {
        if (printfArgBuffer != null) {
            printfArgBuffer.append(" ").append(op).append(" ");
        } else {
            write(" " + op + " ");
        }
    }

    private void writeLine(String code) {
        write(code + "\n");
    }

    private void flushToFile() throws IOException {
        if (writer == null)
            return;
        // Cabeçalhos
        writer.write("#include <stdio.h>\n");
        writer.write("#include <stdlib.h>\n");
        writer.write("#include <stdbool.h>\n\n");
        // Código global (funções)
        writer.write(globalCode.toString());
        // Função main
        writer.write("int main() {\n");
        writer.write(mainCode.toString());
        writer.write("    return 0;\n}\n");
        writer.close();
    }

    public void main() {
        token = getNextToken();
        if (file()) {
            if (token != null && "EOF".equals(token.tipo)) {
                try {
                    flushToFile();
                } catch (IOException e) {
                    System.out.println("Erro ao escrever arquivo: " + e.getMessage());
                }
                if (showTree)
                    ast.printTree();
                return;
            } else {
                erro();
            }
        } else {
            erro();
        }
        try {
            if (writer != null)
                writer.close();
        } catch (IOException e) {
        }
    }

    public Token getNextToken() {
        if (!tokens.isEmpty()) {
            Token nextToken = tokens.remove(0);
            if (nextToken != null && nextToken.linha > currentLine) {
                currentLine = nextToken.linha;
            }
            return nextToken;
        }
        return null;
    }

    private void erro() {
        String errorMsg = "Erro Sintático na linha " + currentLine + ": token '"
                + (token != null ? token.lexema : "EOF") + "' inesperado";
        System.out.println(errorMsg);
        if (currentLine > 0 && currentLine <= codeLines.length) {
            String line = codeLines[currentLine - 1];
            System.out.println(line);
            if (token != null && token.lexema != null) {
                int errorPos = line.indexOf(token.lexema);
                if (errorPos >= 0) {
                    for (int i = 0; i < errorPos; i++)
                        System.out.print(" ");
                    for (int i = 0; i < token.lexema.length(); i++)
                        System.out.print("^");
                    System.out.println();
                }
            }
        }
    }

    // ================= GRAMÁTICA =================

    private boolean file() {
        ast.addRuleNode("file");
        boolean result = bloco() && token != null && "EOF".equals(token.tipo);
        if (result) {
            ast.addTerminalNode("EOF");
            ast.endRuleNode();
        } else {
            ast.endRuleNode();
        }
        return result;
    }

    private boolean bloco() {
        ast.addRuleNode("bloco");
        if (cmd()) {
            bloco();
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return true;
    }

    private boolean cmd() {
        if (token == null)
            return false;
        if (token.tipo.equals("OP_IF"))
            return cmdIf();
        if (token.tipo.equals("OP_FOR"))
            return cmdFor();
        if (token.tipo.equals("OP_WHILE"))
            return cmdWhile();
        if (token.tipo.equals("OP_RETURN"))
            return cmdReturn();
        if (token.tipo.equals("OP_FUNCTION"))
            return cmdDefFunc();
        if (token.tipo.equals("OP_PRINT"))
            return cmdPrint();
        if (token.tipo.equals("OP_INPUT"))
            return cmdInput();
        if (token.tipo.equals("OP_CONTINUE") || token.tipo.equals("OP_BREAK")) {
            boolean ok = matchT("OP_CONTINUE", "continue") || matchT("OP_BREAK", "break");
            if (ok)
                matchT("SEMICOLON", ";");
            return ok;
        }
        boolean temTipo = tipo();
        if (temTipo || (token != null && token.tipo.equals("ID"))) {
            return cmdID();
        }
        return false;
    }

    private boolean cmdID() {
        ast.addRuleNode("cmdID");
        boolean isDecl = (lastType != null);
        if (isDecl) {
            ast.addTerminalNode(lastType, currentLine);
            write(lastType);
            write(" ");
        }
        String varName = id();
        if (varName == null) {
            ast.endRuleNode();
            return false;
        }
        if (isDecl) {
            if (matchT("ASSIGN", "")) {
                // Se o próximo token é '[', é atribuição de lista
                if (token != null && token.tipo.equals("OPEN_BRACKETS")) {
                    write("[] = ");
                } else {
                    write(" = ");
                }
                if (!valor()) {
                    ast.endRuleNode();
                    return false;
                }
            }
        } else {
            // Atribuição: acesso a array opcional e complemento
            acessoListaOp(); // gera os colchetes
            if (!complemento()) {
                ast.endRuleNode();
                return false;
            }
        }
        if (matchT("SEMICOLON", "")) {
            write(";\n");
            if (isDecl)
                symbolTable.put(varName, lastType);
            lastType = null;
            ast.endRuleNode();
            return true;
        }
        lastType = null;
        ast.endRuleNode();
        return false;
    }

    private boolean acessoListaOp() {
        // Lookahead para evitar conflito com lista literal
        if (token != null && token.tipo.equals("OPEN_BRACKETS")) {
            Token savedToken = token;
            List<Token> savedTokens = new ArrayList<>(tokens);
            if (matchT("OPEN_BRACKETS", "[")) {
                if (expressaoAritmetica() && matchT("CLOSE_BRACKETS", "]")) {
                    acessoListaOp();
                    return true;
                }
            }
            token = savedToken;
            tokens = savedTokens;
        }
        return true;
    }

    private boolean acessoLista() {
        if (matchT("OPEN_BRACKETS", "[")) {
            if (expressaoAritmetica() && matchT("CLOSE_BRACKETS", "]")) {
                return true;
            }
        }
        return false;
    }

    private boolean complemento() {
        if (matchT("ASSIGN", "")) {
            write(" = ");
            return valor();
        }
        if (operadorAssignOp())
            return valor();

        if (matchT("OPEN_PARENTHESIS", "")) {
            write("(");
            ast.addRuleNode("args"); // ← abre nó args
            boolean ok = corpoLista() && matchT("CLOSE_PARENTHESIS", "");
            ast.endRuleNode(); // ← fecha nó args
            write(")");
            return ok;
        }
        if (matchT("INCREMENT", "++") || matchT("DECREMENT", "--"))
            return true;
        return true;
    }

    private boolean operadorAssignOp() {
        if (matchT("PLUS_ASSIGN", "+=") || matchT("MINUS_ASSIGN", "-=")
                || matchT("TIMES_ASSIGN", "*=") || matchT("DIV_ASSIGN", "/=")
                || matchT("MOD_ASSIGN", "%=") || matchT("POW_ASSIGN", "^=")) {
            return valor();
        }
        return false;
    }

    private boolean valor() {
        if (token != null && token.tipo.equals("OPEN_BRACKETS")) {
            return lista(); // lista() já consome o [ e o ]
        }
        if (token != null && token.tipo.equals("OP_PRINT"))
            return cmdPrint();
        if (token != null && token.tipo.equals("OP_INPUT"))
            return cmdInput();
        return expressaoLogica();
    }

    private boolean expressaoLogica() {
        if (!expressaoRelacional())
            return false;
        while (token != null && (token.tipo.equals("AND") || token.tipo.equals("OR"))) {
            String op = token.tipo.equals("AND") ? "&&" : "||";
            if (token.linha > currentLine)
                currentLine = token.linha;
            ast.addTerminalNode(op, currentLine);
            token = getNextToken();
            writeOp(op);
            if (!expressaoRelacional())
                return false;
        }
        return true;
    }

    private boolean expressaoRelacional() {
        if (!expressaoAritmetica())
            return false;
        while (op_comparacao()) {
            if (!expressaoAritmetica())
                return false;
        }
        return true;
    }

    private boolean op_comparacao() {
        String[] types = {"GREATER", "LESS", "EQUAL", "DIFFERENT", "GREATER_EQUAL", "LESS_EQUAL"};
        String[] ops = {">", "<", "==", "!=", ">=", "<="};
        for (int i = 0; i < types.length; i++) {
            if (token != null && token.tipo.equals(types[i])) {
                if (token.linha > currentLine)
                    currentLine = token.linha;
                ast.addTerminalNode(ops[i], currentLine);
                token = getNextToken();
                writeOp(ops[i]);
                return true;
            }
        }
        return false;
    }

    private boolean expressaoAritmetica() {
        if (!termo())
            return false;
        while (op_adicao()) {
            if (!termo())
                return false;
        }
        return true;
    }

    private boolean op_adicao() {
        if (token != null && (token.tipo.equals("PLUS") || token.tipo.equals("MINUS"))) {
            String op = token.tipo.equals("PLUS") ? "+" : "-";
            if (token.linha > currentLine)
                currentLine = token.linha;
            ast.addTerminalNode(op, currentLine);
            token = getNextToken();
            writeOp(op);
            return true;
        }
        return false;
    }

    private boolean op_mult() {
        if (token != null && (token.tipo.equals("TIMES") || token.tipo.equals("DIV")
                || token.tipo.equals("MOD"))) {
            String op = token.tipo.equals("TIMES") ? "*" : token.tipo.equals("DIV") ? "/" : "%";
            if (token.linha > currentLine)
                currentLine = token.linha;
            ast.addTerminalNode(op, currentLine);
            token = getNextToken();
            writeOp(op);
            return true;
        }
        return false;
    }

    private boolean termo() {
        if (!fator())
            return false;
        while (op_mult()) {
            if (!fator())
                return false;
        }
        return true;
    }

    private boolean fator() {
        if (!elemento())
            return false;
        while (matchT("POW", "^")) {
            if (!elemento())
                return false;
        }
        return true;
    }

    private boolean elemento() {
        if (matchT("INCREMENT", "++") || matchT("DECREMENT", "--")) {
            String varName = id();
            if (varName != null)
                return X();
            return false;
        }
        String varName = id();
        if (varName != null)
            return X();

        if (token != null && token.tipo.equals("INT")) {
            String lex = token.lexema;
            matchT("INT", lex);
            return true;
        }
        if (token != null && token.tipo.equals("FLOAT")) {
            String lex = token.lexema;
            matchT("FLOAT", lex);
            return true;
        }
        if (token != null && token.tipo.equals("STR")) {
            String lex = token.lexema;
            matchT("STR", "\"" + lex + "\"");
            return true;
        }
        if (token != null && token.tipo.equals("BOOL")) {
            String lex = token.lexema;
            matchT("BOOL", lex);
            return true;
        }
        if (matchT("OPEN_PARENTHESIS", "(")) {
            boolean ok = expressaoLogica() && matchT("CLOSE_PARENTHESIS", ")");
            return ok;
        }
        return false;
    }

    private boolean X() {
        if (matchT("INCREMENT", "++") || matchT("DECREMENT", "--"))
            return true;
        if (token != null
                && (token.tipo.equals("OPEN_BRACKETS") || token.tipo.equals("OPEN_PARENTHESIS"))) {
            return composicao() && X();
        }
        return true;
    }

    private boolean composicao() {
        if (matchT("OPEN_BRACKETS", "[")) {
            boolean ok = expressaoAritmetica() && matchT("CLOSE_BRACKETS", "]") && acessoListaOp();
            return ok;
        }
        if (matchT("OPEN_PARENTHESIS", "(")) {
            boolean ok = corpoLista() && matchT("CLOSE_PARENTHESIS", ")");
            return ok;
        }
        return false;
    }

    private boolean corpoLista() {
        if (token != null && (token.tipo.equals("CLOSE_BRACKETS")
                || token.tipo.equals("CLOSE_PARENTHESIS"))) {
            return true;
        }
        if (valor())
            return entradaLista();
        return true;
    }

    private boolean entradaLista() {
        if (matchT("COMMA", "")) { // consome sem adicionar à AST
            write(", "); // escreve no C normalmente
            return valor() && entradaLista();
        }
        return true;
    }

    private boolean lista() {
        if (matchT("OPEN_BRACKETS", "")) { // consome [ sem escrever
            write("{");
            boolean ok = corpoLista() && matchT("CLOSE_BRACKETS", ""); // consome ] sem escrever
            write("}");
            return ok;
        }
        return false;
    }

    private boolean cmdIf() {
        ast.addRuleNode("cmdIf");
        if (matchT("OP_IF", "if")) {
            write("(");
            if (matchT("OPEN_PARENTHESIS", "")) {
                if (valor() && matchT("CLOSE_PARENTHESIS", "")) {
                    write(") {\n");
                    if (matchT("OPEN_BRACES", "") && bloco() && matchT("CLOSE_BRACES", "")) {
                        write("}");
                        if (cmdElse()) {
                            ast.endRuleNode();
                            return true;
                        }
                    }
                }
            }
        }
        ast.endRuleNode();
        return false;
    }

    private boolean cmdElse() {
        if (matchT("OP_ELSE", "")) {
            write(" else {\n");
            if (matchT("OPEN_BRACES", "") && bloco() && matchT("CLOSE_BRACES", "")) {
                write("}");
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean cmdWhile() {
        ast.addRuleNode("cmdWhile");
        if (matchT("OP_WHILE", "while")) {
            write("(");
            if (matchT("OPEN_PARENTHESIS", "")) {
                if (valor() && matchT("CLOSE_PARENTHESIS", "")) {
                    write(") {\n");
                    if (matchT("OPEN_BRACES", "") && bloco() && matchT("CLOSE_BRACES", "")) {
                        write("}");
                        ast.endRuleNode();
                        return true;
                    }
                }
            }
        }
        ast.endRuleNode();
        return false;
    }

    private boolean cmdFor() {
        ast.addRuleNode("cmdFor");
        if (matchT("OP_FOR", "for")) {
            write("(");
            if (matchT("OPEN_PARENTHESIS", "")) {
                // Inicialização
                if (!variavelFor())
                    return false;
                // Condição
                if (matchT("SEMICOLON", "")) {
                    write("; ");
                    if (!expressaoRelacional())
                        return false;
                    // Incremento
                    if (matchT("SEMICOLON", "")) {
                        write("; ");
                        if (!expressaoAritmetica())
                            return false;
                        if (matchT("CLOSE_PARENTHESIS", "")) {
                            write(") {\n");
                            if (matchT("OPEN_BRACES", "") && bloco()
                                    && matchT("CLOSE_BRACES", "")) {
                                write("}\n");
                                ast.endRuleNode();
                                return true;
                            }
                        }
                    }
                }
            }
        }
        ast.endRuleNode();
        return false;
    }

    private boolean variavelFor() {
        if (tipo()) {
            ast.addTerminalNode(lastType, currentLine);
            write(lastType);
            write(" ");
            String varName = id();
            if (varName != null) {
                if (matchT("ASSIGN", "")) {
                    write(" = ");
                    if (expressaoAritmetica()) {
                        symbolTable.put(varName, lastType);
                        return true;
                    }
                } else {
                    symbolTable.put(varName, lastType);
                    return true;
                }
            }
            lastType = null;
            return false;
        }
        String varName = id();
        if (varName != null) {
            if (matchT("ASSIGN", "")) {
                write(" = ");
                return expressaoAritmetica();
            }
            return true;
        }
        return false;
    }

    private boolean cmdDefFunc() {
        ast.addRuleNode("cmdDefFunc");
        insideFunction = true;
        if (matchT("OP_FUNCTION", "")) {
            // Tipo de retorno opcional após функция (ex: интеграл → int); padrão void
            String returnType = "void";
            if (tipo()) {
                returnType = (lastType != null) ? lastType : "void";
                lastType = null;
            }
            ast.addTerminalNode(returnType, currentLine);
            write(returnType + " ");
            String funcName = id();
            if (funcName != null) {
                write("(");
                if (matchT("OPEN_PARENTHESIS", "")) {
                    if (listaParametros() && matchT("CLOSE_PARENTHESIS", "")) {
                        write(") {\n");
                        if (matchT("OPEN_BRACES", "")) {
                            if (bloco() && matchT("CLOSE_BRACES", "")) {
                                write("}\n\n");
                                insideFunction = false;
                                ast.endRuleNode();
                                return true;
                            }
                        }
                    }
                }
            }
        }
        insideFunction = false;
        ast.endRuleNode();
        return false;
    }

    private boolean listaParametros() {
        tipo(); // consome o tipo se presente (ex: интеграл → "int")
        String paramType = lastType;
        if (lastType != null) {
            write(lastType + " ");
            lastType = null;
        }
        String paramName = id();
        if (paramName != null) {
            symbolTable.put(paramName, paramType != null ? paramType : "int"); // default int if no
                                                                               // type
            return entradaListaParam();
        }
        return true;
    }

    private boolean entradaListaParam() {
        if (matchT("COMMA", "")) {
            write(", ");
            tipo(); // tipo opcional antes de cada parâmetro
            String paramType = lastType;
            if (lastType != null) {
                write(lastType + " ");
                lastType = null;
            }
            String paramName = id();
            if (paramName != null) {
                symbolTable.put(paramName, paramType != null ? paramType : "int");
                return entradaListaParam();
            }
            return false;
        }
        return true;
    }

    private boolean cmdReturn() {
        ast.addRuleNode("cmdReturn");
        if (matchT("OP_RETURN", "")) {
            write("return ");
            if (valorRetorno()) {
                write(";\n");
                ast.endRuleNode();
                return true;
            }
        }
        ast.endRuleNode();
        return false;
    }

    private boolean valorRetorno() {
        if (token == null || token.tipo.equals("EOF") || token.tipo.equals("CLOSE_BRACES")
                || token.tipo.equals("OP_CONTINUE") || token.tipo.equals("OP_BREAK")) {
            return true;
        }
        write(" ");
        return valor();
    }

    private boolean cmdPrint() {
        ast.addRuleNode("cmdPrint");
        if (matchT("OP_PRINT", "") && matchT("OPEN_PARENTHESIS", "")) {
            printfArgBuffer = new StringBuilder();
            boolean ok = corpoLista();
            String arg = printfArgBuffer.toString().trim();
            printfArgBuffer = null;
            if (ok && matchT("CLOSE_PARENTHESIS", "") && matchT("SEMICOLON", "")) {
                // Determinar o tipo do argumento para usar o % correto
                if (arg.matches("[a-zA-Z_][a-zA-Z0-9_]*")) { // é um identificador
                    String type = symbolTable.get(arg);
                    if (type != null) {
                        if (type.equals("int")) {
                            write("printf(\"%d\\n\", " + arg + ");\n");
                        } else if (type.equals("float")) {
                            write("printf(\"%f\\n\", " + arg + ");\n");
                        } else if (type.equals("bool")) {
                            write("printf(\"%d\\n\", " + arg + ");\n");
                        } else if (type.equals("char*")) {
                            write("printf(\"%s\\n\", " + arg + ");\n");
                        } else {
                            // tipo desconhecido, usa %d por padrão
                            write("printf(\"%d\\n\", " + arg + ");\n");
                        }
                    } else {
                        // não encontrado, usa %d
                        write("printf(\"%d\\n\", " + arg + ");\n");
                    }
                } else {
                    // literal
                    if (arg.startsWith("\"")) {
                        // String literal: embute \n antes de fechar as aspas
                        String withNewline = arg.substring(0, arg.length() - 1) + "\\n\"";
                        write("printf(" + withNewline + ");\n");
                    } else if (arg.matches("-?[0-9]+\\.[0-9]+")) {
                        // Float literal
                        write("printf(\"%f\\n\", " + arg + ");\n");
                    } else {
                        // Int literal ou outro, usa %d
                        write("printf(\"%d\\n\", " + arg + ");\n");
                    }
                }
                ast.endRuleNode();
                return true;
            }
        }
        ast.endRuleNode();
        return false;
    }

    // Sobrecarga para escrever com formato (usado no printf)
    private void write(String format, String arg) {
        write(String.format(format, arg));
    }

    private boolean cmdInput() {
        ast.addRuleNode("cmdInput");
        if (matchT("OP_INPUT", "scanf")) {
            if (matchT("OPEN_PARENTHESIS", "")) {
                scanInputMode = true;
                scanfFormatBuffer = new StringBuilder();
                scanfArgsBuffer = new StringBuilder();
                boolean ok = scanInputArgs();
                scanInputMode = false;
                if (ok && matchT("CLOSE_PARENTHESIS", "") && matchT("SEMICOLON", "")) {
                    String format = scanfFormatBuffer.toString().trim();
                    String args = scanfArgsBuffer.toString();
                    write("(\"" + format + "\"");
                    if (!args.isEmpty()) {
                        write(", " + args);
                    }
                    write(");\n");
                    ast.endRuleNode();
                    return true;
                }
            }
        }
        ast.endRuleNode();
        return false;
    }

    private boolean scanInputArgs() {
        if (token != null && token.tipo.equals("ID")) {
            addScanArg(token.lexema);
            matchT("ID", token.lexema);
            while (matchT("COMMA", ",")) {
                if (token != null && token.tipo.equals("ID")) {
                    addScanArg(token.lexema);
                    matchT("ID", token.lexema);
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void addScanArg(String varName) {
        if (scanfFormatBuffer.length() > 0)
            scanfFormatBuffer.append(" ");
        scanfFormatBuffer.append("%d");
        if (scanfArgsBuffer.length() > 0)
            scanfArgsBuffer.append(", ");
        scanfArgsBuffer.append("&").append(varName);
    }

    private boolean tipo() {
        if (matchT("INT_TYPE", "")) {
            lastType = "int";
            return true;
        }
        if (matchT("FLOAT_TYPE", "")) {
            lastType = "float";
            return true;
        }
        if (matchT("BOOL_TYPE", "")) {
            lastType = "bool";
            return true;
        }
        if (matchT("STR_TYPE", "")) {
            lastType = "char*";
            return true;
        }
        return false;
    }

    private String id() {
        if (token != null && token.tipo.equals("ID")) {
            String lexema = token.lexema;
            matchT("ID", lexema);
            return lexema;
        }
        return null;
    }

    // ================= MATCH COM ESCRITA INTELIGENTE =================
    private void traduz(String code) {
        write(code);
    }

    private boolean matchT(String palavra) {
        if (token != null && token.tipo.equals(palavra)) {
            if (token.linha > currentLine)
                currentLine = token.linha;
            token = getNextToken();
            return true;
        }
        return false;
    }

    private boolean matchT(String palavra, String newcode) {
        if (token != null && token.tipo.equals(palavra)) {
            if (token.linha > currentLine)
                currentLine = token.linha;

            // Se estamos capturando para o printf, armazena no buffer
            if (printfArgBuffer != null) {
                if (!newcode.isEmpty()) {
                    if (printfArgBuffer.length() > 0) {
                        char last = printfArgBuffer.charAt(printfArgBuffer.length() - 1);
                        if (last != ' ' && last != '(' && !newcode.equals(")")
                                && !newcode.equals(",") && !newcode.equals("("))
                            printfArgBuffer.append(" ");
                    }
                    printfArgBuffer.append(newcode);
                }
            } else if (!newcode.isEmpty()) {
                if (!scanInputMode) {
                    StringBuilder lastBuff = insideFunction ? globalCode : mainCode;
                    if (lastBuff.length() > 0) {
                        char last = lastBuff.charAt(lastBuff.length() - 1);
                        // Add a space only between two word-like tokens
                        boolean lastIsWord = Character.isLetterOrDigit(last) || last == '_';
                        boolean nextIsWord = newcode.length() > 0
                                && (Character.isLetterOrDigit(newcode.charAt(0))
                                        || newcode.charAt(0) == '_');
                        if (lastIsWord && nextIsWord)
                            write(" ");
                    }
                    write(newcode);
                }
            }

            if (!newcode.isEmpty())
                ast.addTerminalNode(newcode, currentLine);
            token = getNextToken();
            return true;
        }
        return false;
    }

    public Tree getAst() {
        return ast;
    }
}
