package sintatico;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import lexer.Token;

public class Parser {
    List<Token> tokens;
    Token token;
    private BufferedWriter writer;
    private String outFilePath;
    private String lastType = null; // Rastreia o tipo da variável sendo declarada
    private String[] codeLines; // Armazena as linhas do código fonte
    private int currentLine = 1; // Rastreia a linha atual
    private Tree ast; // Árvore sintática
    private boolean showTree; // Controla se a AST deve ser impressa

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

    // ================= FUNÇÃO PRINCIPAL =================
    public void main() {
        String headers = "#include <stdio.h>\n#include <stdlib.h>\n#include <stdbool.h>\n";
        String mainStart = "int main(){\n";
        write(headers);
        write(mainStart);
        token = getNextToken();
        if (file()) {
            if (token != null && "EOF".equals(token.tipo)) {
                String ret = "\nreturn 0;\n}\n";
                writeLine(ret);
                closeWriter();
                if (showTree)
                    ast.printTree();
                return;
            } else {
                erro();
            }
        } else {
            erro();
        }
        closeWriter();
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

    // ================================== GRAMÁTICA ==================================
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
            return matchT("OP_CONTINUE", "continue") || matchT("OP_BREAK", "break");
        }

        boolean temTipo = tipo();
        if (temTipo || (token != null && token.tipo.equals("ID"))) {
            return cmdID();
        }
        return false;
    }

    private boolean cmdID() {
        ast.addRuleNode("cmdID");
        if (lastType != null) {
            ast.addTerminalNode(lastType, currentLine);
        }
        if (id() && acessoListaOp() && complemento()) {
            lastType = null;
            if (matchT("SEMICOLON", ";")) {
                ast.endRuleNode();
                return true;
            }
        }
        lastType = null;
        ast.endRuleNode();
        return false;
    }

    private boolean acessoListaOp() {
        if (matchT("OPEN_BRACKETS", "[")) {
            return acessoLista() && acessoListaOp();
        }
        return true;
    }

    private boolean acessoLista() {
        if (matchT("OPEN_BRACKETS", "[") && expressaoAritmetica()
                && matchT("CLOSE_BRACKETS", "]")) {
            return true;
        }
        return false;
    }

    private boolean complemento() {
        if (matchT("ASSIGN", "="))
            return valor();
        if (operadorAssignOp())
            return valor();
        if (matchT("OPEN_PARENTHESIS", "("))
            return corpoLista() && matchT("CLOSE_PARENTHESIS", ")");

        // Suporte a incremento/decremento pós-fixado: var++; ou var--;
        if (matchT("INCREMENT", "++") || matchT("DECREMENT", "--"))
            return true;

        return true;
    }

    private boolean operadorAssignOp() {
        if (matchT("PLUS_ASSIGN", "+=") || matchT("MINUS_ASSIGN", "-=")
                || matchT("TIMES_ASSIGN", "*=") || matchT("DIV_ASSIGN", "/=")
                || matchT("MOD_ASSIGN", "%=") || matchT("POW_ASSIGN", "^=")) {
            return true;
        }
        return false;
    }

    private boolean valor() {
        if (matchT("OPEN_BRACKETS", "["))
            return lista();
        if (token != null && token.tipo.equals("OP_PRINT"))
            return cmdPrint();
        if (token != null && token.tipo.equals("OP_INPUT"))
            return cmdInput();
        return expressaoLogica();
    }

    private boolean expressaoLogica() {
        if (!expressaoRelacional())
            return false;
        while (matchT("AND", "&&") || matchT("OR", "||")) {
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
        if (matchT("GREATER", ">") || matchT("LESS", "<") || matchT("EQUAL", "==")
                || matchT("DIFFERENT", "!=") || matchT("GREATER_EQUAL", ">=")
                || matchT("LESS_EQUAL", "<=")) {
            return true;
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
        if (matchT("PLUS", "+") || matchT("MINUS", "-"))
            return true;
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

    private boolean op_mult() {
        if (matchT("TIMES", "*") || matchT("DIV", "/") || matchT("MOD", "%"))
            return true;
        return false;
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
            if (id())
                return X();
            return false;
        }
        if (id())
            return X();

        if (token != null && token.tipo.equals("INT")) {
            String lex = token.lexema;
            matchT("INT", lex);
            ast.addTerminalNode(lex);
            return true;
        }
        if (token != null && token.tipo.equals("FLOAT")) {
            String lex = token.lexema;
            matchT("FLOAT", lex);
            ast.addTerminalNode(lex);
            return true;
        }
        if (token != null && token.tipo.equals("STR")) {
            String lex = token.lexema;
            matchT("STR", "\"" + lex + "\"");
            ast.addTerminalNode("\"" + lex + "\"");
            return true;
        }
        if (token != null && token.tipo.equals("BOOL")) {
            String lex = token.lexema;
            matchT("BOOL", lex);
            ast.addTerminalNode(lex);
            return true;
        }
        if (matchT("OPEN_PARENTHESIS", "(")) {
            return expressaoLogica() && matchT("CLOSE_PARENTHESIS", ")");
        }
        return false;
    }

    private boolean X() {
        if (matchT("INCREMENT", "++") || matchT("DECREMENT", "--"))
            return true;
        if (matchT("OPEN_BRACKETS", "[") || matchT("OPEN_PARENTHESIS", "(")) {
            return composicao() && X();
        }
        return true;
    }

    private boolean composicao() {
        if (matchT("OPEN_BRACKETS", "[")) {
            return expressaoAritmetica() && matchT("CLOSE_BRACKETS", "]") && acessoListaOp();
        }
        if (matchT("OPEN_PARENTHESIS", "(")) {
            return corpoLista() && matchT("CLOSE_PARENTHESIS", ")");
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
        if (matchT("COMMA", ","))
            return valor() && entradaLista();
        return true;
    }

    private boolean lista() {
        if (matchT("OPEN_BRACKETS", "[")) {
            return corpoLista() && matchT("CLOSE_BRACKETS", "]");
        }
        return false;
    }

    private boolean cmdIf() {
        ast.addRuleNode("cmdIf");
        if (matchT("OP_IF", "if") && valor() && matchT("OPEN_BRACES", "{") && bloco()
                && matchT("CLOSE_BRACES", "}") && cmdElse()) {
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    private boolean cmdElse() {
        if (matchT("OP_ELSE", "else") && matchT("OPEN_BRACES", "{") && bloco()
                && matchT("CLOSE_BRACES", "}")) {
            return true;
        }
        return true;
    }

    private boolean cmdWhile() {
        ast.addRuleNode("cmdWhile");
        if (matchT("OP_WHILE", "while") && valor() && matchT("OPEN_BRACES", "{") && bloco()
                && matchT("CLOSE_BRACES", "}")) {
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    private boolean cmdFor() {
        ast.addRuleNode("cmdFor");
        if (matchT("OP_FOR", "for") && matchT("OPEN_PARENTHESIS", "(") && variavelFor()
                && matchT("SEMICOLON", ";") && expressaoRelacional() && matchT("SEMICOLON", ";")
                && expressaoAritmetica() && matchT("CLOSE_PARENTHESIS", ")")
                && matchT("OPEN_BRACES", "{") && bloco() && matchT("CLOSE_BRACES", "}")) {
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    private boolean variavelFor() {
        if (tipo()) {
            if (id() && complemento()) {
                lastType = null;
                return true;
            }
            lastType = null;
            return false;
        }
        if (id() && complemento())
            return true;
        return false;
    }

    private boolean cmdDefFunc() {
        ast.addRuleNode("cmdDefFunc");
        if (matchT("OP_FUNCTION", "void") && id() && matchT("OPEN_PARENTHESIS", "(")
                && listaParametros() && matchT("CLOSE_PARENTHESIS", ")")
                && matchT("OPEN_BRACES", "{") && bloco() && matchT("CLOSE_BRACES", "}")) {
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    private boolean listaParametros() {
        if (id())
            return entradaListaParam();
        return true;
    }

    private boolean entradaListaParam() {
        if (matchT("COMMA", ","))
            return id() && entradaListaParam();
        return true;
    }

    private boolean cmdReturn() {
        ast.addRuleNode("cmdReturn");
        if (matchT("OP_RETURN", "return")) {
            valorRetorno();
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    private boolean valorRetorno() {
        if (token == null || token.tipo.equals("EOF") || token.tipo.equals("CLOSE_BRACES")
                || token.tipo.equals("OP_CONTINUE") || token.tipo.equals("OP_BREAK")) {
            return true;
        }
        return valor();
    }

    private boolean cmdPrint() {
        ast.addRuleNode("cmdPrint");
        if (matchT("OP_PRINT", "printf") && matchT("OPEN_PARENTHESIS", "(") && corpoLista()
                && matchT("CLOSE_PARENTHESIS", ")") && matchT("SEMICOLON", ";")) {
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    private boolean cmdInput() {
        ast.addRuleNode("cmdInput");
        if (matchT("OP_INPUT", "scanf") && matchT("OPEN_PARENTHESIS", "(") && corpoLista()
                && matchT("CLOSE_PARENTHESIS", ")") && matchT("SEMICOLON", ";")) {
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    private boolean tipo() {
        if (matchT("INT_TYPE", "int")) {
            lastType = "int";
            return true;
        }
        if (matchT("FLOAT_TYPE", "float")) {
            lastType = "float";
            return true;
        }
        if (matchT("BOOL_TYPE", "bool")) {
            lastType = "bool";
            return true;
        }
        if (matchT("STR_TYPE", "char*")) {
            lastType = "char*";
            return true;
        }
        return false;
    }

    private boolean id() {
        if (token != null && token.tipo.equals("ID")) {
            String lexema = token.lexema;
            matchT("ID", lexema);
            return true;
        }
        return false;
    }

    // ================= ESCRITA E MATCH =================
    private void traduz(String code) {
        write(code);
    }

    private void write(String code) {
        if (this.writer == null)
            return;
        try {
            this.writer.write(code);
        } catch (IOException e) {
            System.out.println("Erro ao escrever: " + e.getMessage());
        }
    }

    private void writeLine(String code) {
        write(code);
    }

    private void closeWriter() {
        if (this.writer == null)
            return;
        try {
            this.writer.close();
        } catch (IOException e) {
            System.out.println("Erro ao fechar: " + e.getMessage());
        }
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
            traduz(newcode);
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
