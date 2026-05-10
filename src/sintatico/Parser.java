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
    private String lastType = null;  // Rastreia o tipo da variável sendo declarada
    private String[] codeLines;  // Armazena as linhas do código fonte
    private int currentLine = 1;  // Rastreia a linha atual
    private Tree ast;  // Árvore sintática
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

    public Parser(List<Token> tokens, String inputFilePath, String sourceCode, boolean showTree, String outputDir) {
        this.tokens = tokens;
        this.codeLines = sourceCode.split("\n");
        this.ast = new Tree();  // Inicializa a árvore sintática
        this.showTree = showTree;
        try{
            String outName = new File(inputFilePath).getName();
            if(outName.endsWith(".emp")){
                outName = outName.substring(0, outName.length()-4) + ".c";
            } else {
                outName = outName + ".c";
            }
            
            // Define o caminho de saída
            if(outputDir != null && !outputDir.isEmpty()) {
                File outDirFile = new File(outputDir);
                if(!outDirFile.exists()) {
                    outDirFile.mkdirs();
                }
                this.outFilePath = outputDir + File.separator + outName;
            } else {
                this.outFilePath = outName;
            }
            
            File outFile = new File(outFilePath);
            this.writer = new BufferedWriter(new FileWriter(outFile));
        } catch (IOException e){
            System.out.println("Erro ao criar arquivo de saída: " + e.getMessage());
            this.writer = null;
        }
    }
    // ================= FUNÇÃO PRINCIPAL =================
    public void main(){
        String headers = "#include <stdio.h>\n#include <stdlib.h>\n#include <stdbool.h>\n";
        String mainStart = "int main(){\n";
        // System.out.print(headers + mainStart);  DEBUG
        write(headers);
        write(mainStart);
        token = getNextToken();
        if(file()){
            if(token != null && "EOF".equals(token.tipo)){
                String ret = "\nreturn 0;\n}\n";
                // System.out.print(ret);  DEBUG
                writeLine(ret);
                closeWriter();
                if (showTree) {
                    ast.printTree();  // Exibe a árvore sintática
                }
                return;
            }
            else{
                erro();
            }
        }
        erro();
        closeWriter();
    }

    public Token getNextToken(){
        if(!tokens.isEmpty()){
            Token nextToken = tokens.remove(0);
            if(nextToken != null && nextToken.linha > currentLine){
                currentLine = nextToken.linha;
            }
            return nextToken;
        }
        else{
            return null;
        }
    }
    
    private void erro(){
        String errorMsg = "Erro Sintático na linha " + currentLine + ": token '" + 
                         (token != null ? token.lexema : "EOF") + "' inesperado";
        System.out.println(errorMsg);
        
        // Exibir a linha do código com o erro destacado
        if(currentLine > 0 && currentLine <= codeLines.length){
            String line = codeLines[currentLine - 1];
            System.out.println(line);
            
            // Destacar a posição do erro
            if(token != null && token.lexema != null){
                int errorPos = line.indexOf(token.lexema);
                if(errorPos >= 0){
                    for(int i = 0; i < errorPos; i++){
                        System.out.print(" ");
                    }
                    for(int i = 0; i < token.lexema.length(); i++){
                        System.out.print("^");
                    }
                    System.out.println();
                }
            }
        }
    }

    // ================================== GRAMÁTICA ==================================

    // ================= ARQUIVO =================
    // file -> bloco EOF
    private boolean file(){
        ast.addRuleNode("file");
        boolean result = bloco() && token != null && token.tipo.equals("EOF");
        if (result) {
            ast.addTerminalNode("EOF");
            ast.endRuleNode();
        } else {
            ast.endRuleNode();
        }
        return result;
    }

    // ================= BLOCO DE CÓDIGO =================
    // bloco -> cmd bloco | ε
    private boolean bloco(){
        ast.addRuleNode("bloco");
        if (cmd()){
            bloco();
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return true;
    }

    // ================= COMANDOS =================
    // cmd -> cmdIf | cmdFor | cmdWhile | cmdReturn | cmdDefFunc | cmdPrint | cmdInput | cmdID | RESERVED_PASSE | RESERVED_PARE
    private boolean cmd(){
        if (token == null) return false;
        
        // Tenta executar o comando específico baseado no tipo do token
        if (token.tipo.equals("OP_IF")) return cmdIf();
        if (token.tipo.equals("OP_FOR")) return cmdFor();
        if (token.tipo.equals("OP_WHILE")) return cmdWhile();
        if (token.tipo.equals("OP_RETURN")) return cmdReturn();
        if (token.tipo.equals("OP_FUNCTION")) return cmdDefFunc();
        if (token.tipo.equals("OP_PRINT")) return cmdPrint();
        if (token.tipo.equals("OP_INPUT")) return cmdInput();
        if (token.tipo.equals("OP_CONTINUE") || token.tipo.equals("OP_BREAK")){
            return matchT("OP_CONTINUE", "continue") || matchT("OP_BREAK", "break");
        }
        if (tipo()) return cmdID();
        
        return false;
    }

    // ================= COMANDO ID (ATRIBUIÇÃO) =================
    // cmdID -> tipo ID acessoListaOp complemento ; | ID acessoListaOp complemento ;
    private boolean cmdID(){
        ast.addRuleNode("cmdID");
        // Tenta declaração com tipo
        if (id() && acessoListaOp() && complemento()){
            lastType = null;  // Reseta o tipo após usar
            if (matchT("SEMICOLON", ";")) {
                ast.endRuleNode();
                return true;
            }
        }
        lastType = null;  // Reseta o tipo em caso de erro
        ast.endRuleNode();
        return false;
    }

    // ================= ACESSO LISTA OPERADOR =================
    // acessoListaOp -> acessoLista acessoListaOp | ε
    private boolean acessoListaOp(){
        if (matchT("OPEN_BRACKETS", "[")){
            return acessoLista() && acessoListaOp();
        }
        return true;
    }

    // ================= ACESSO LISTA =================
    // acessoLista -> OPEN_BRACKET expressaoAritmetica CLOSE_BRACKET
    private boolean acessoLista(){
        if (matchT("OPEN_BRACKETS", "[") && expressaoAritmetica() && matchT("CLOSE_BRACKETS", "]")){
            return true;
        }
        return false;
    }

    // ================= COMPLEMENTO =================
    // complemento -> atribComum | atribComOp | chamadaFuncao | ε
    private boolean complemento(){
        if (matchT("ASSIGN", "=")){
            return valor();
        }
        if (operadorAssignOp()){
            return valor();
        }
        if (matchT("OPEN_PARENTHESIS", "(")){
            boolean result = corpoLista() && matchT("CLOSE_PARENTHESIS", ")");
            return result;
        }
        return true;
    }

    // ================= OPERADOR DE ATRIBUIÇÃO =================
    // assignOp -> += | -= | *= | /= | %= | ^=
    private boolean operadorAssignOp(){
        if (matchT("PLUS_ASSIGN", "+=") ||
            matchT("MINUS_ASSIGN", "-=") ||
            matchT("TIMES_ASSIGN", "*=") ||
            matchT("DIV_ASSIGN", "/=") ||
            matchT("MOD_ASSIGN", "%=") ||
            matchT("POW_ASSIGN", "^=")){
            return true;
        }
        return false;
    }

    // ================= VALOR =================
    // valor -> expressaoLogica | lista | cmdPrint | cmdInput
    private boolean valor(){
        if (matchT("OPEN_BRACKETS", "[")){
            return lista();
        }
        if (token.tipo.equals("OP_PRINT")){
            return cmdPrint();
        }
        if (token.tipo.equals("OP_INPUT")){
            return cmdInput();
        }
        return expressaoLogica();
    }

    // ================= EXPRESSÃO LÓGICA =================
    // expressaoLogica -> expressaoRelacional (AND|OR expressaoRelacional)*
    private boolean expressaoLogica(){   
        if (!expressaoRelacional()){
            return false;
        }
        while (matchT("AND", "&&") || matchT("OR", "||")){
            if (!expressaoRelacional()){
                return false;
            }
        }
        return true;
    }

    // ================= EXPRESSÃO RELACIONAL =================
    // expressaoRelacional -> expressaoAritmetica (opRelacional expressaoAritmetica)*
    private boolean expressaoRelacional(){   
        if (!expressaoAritmetica()){
            return false;
        }
        while (op_comparacao()){
            if (!expressaoAritmetica()){
                return false;
            }
        }
        return true;
    }

    // ================= OPERADOR DE COMPARAÇÃO =================
    private boolean op_comparacao(){
        if (matchT("GREATER", ">") || 
            matchT("LESS", "<") ||     
            matchT("EQUAL", "==") ||
            matchT("DIFFERENT", "!=") ||
            matchT("GREATER_EQUAL", ">=") ||
            matchT("LESS_EQUAL", "<=")){
            return true;
        }
        return false;
    }

    // ================= EXPRESSÃO ARITMÉTICA =================
    // expressaoAritmetica -> termo (opAd termo)*
    private boolean expressaoAritmetica(){   
        if (!termo()){
            return false;
        }
        while (op_adicao()){
            if (!termo()){
                return false;
            }
        }
        return true;
    }

    // ================= OPERADOR DE ADIÇÃO =================
    private boolean op_adicao(){
        if (matchT("PLUS", "+") || 
            matchT("MINUS", "-")){
            return true;
        }
        return false;
    }

    // ================= TERMO =================
    // termo -> fator (opMul fator)*
    private boolean termo(){   
        if (!fator()){
            return false;
        }
        while (op_mult()){
            if (!fator()){
                return false;
            }
        }
        return true;
    }

    // ================= OPERADOR DE MULTIPLICAÇÃO =================
    private boolean op_mult(){
        if (matchT("TIMES", "*") || 
            matchT("DIV", "/") ||
            matchT("MOD", "%")){
            return true;
        }
        return false;
    }

    // ================= FATOR =================
    // fator -> elemento (POW elemento)*
    private boolean fator(){   
        if (!elemento()){
            return false;
        }
        while (matchT("POW", "^")){
            if (!elemento()){
                return false;
            }
        }
        return true;
    }

    // ================= ELEMENTO =================
    // elemento -> (INCREMENT | DECREMENT) ID | ID X | NUM | STRING | BOOL | (expressaoLogica)
    private boolean elemento(){
        if (matchT("INCREMENT", "++") || matchT("DECREMENT", "--")){
            if (id()){
                return X();
            }
            return false;
        }
        if (id()){
            return X();
        }
        if (matchT("INT", token.lexema) || 
            matchT("FLOAT", token.lexema)){
            return true;
        }
        // Adiciona aspas
        if (token.tipo.equals("STR")){
            matchT("STR", "\"" + token.lexema + "\"");
            return true;
        }
        if (matchT("BOOL", token.lexema)){
            return true;
        }
        if (matchT("OPEN_PARENTHESIS", "(")){
            return expressaoLogica() && matchT("CLOSE_PARENTHESIS", ")");
        }
        return false;
    }

    // ================= X (COMPOSIÇÃO) =================
    // X -> composicao X | (INCREMENT | DECREMENT) | ε
    private boolean X(){
        // Pós-fixos (var++, var--)
        if (matchT("INCREMENT", "++") || matchT("DECREMENT", "--")){
            return true;
        }
        // Composição (acesso a arrays ou chamada de função)
        if (matchT("OPEN_BRACKETS", "[") || matchT("OPEN_PARENTHESIS", "(")){
            return composicao() && X();
        }
        return true;
    }

    // ================= COMPOSIÇÃO =================
    // composicao -> acessoLista acessoListaOp | chamadaFuncao
    private boolean composicao(){
        if (matchT("OPEN_BRACKETS", "[")){
            return expressaoAritmetica() && matchT("CLOSE_BRACKETS", "]") && acessoListaOp();
        }
        if (matchT("OPEN_PARENTHESIS", "(")){
            return corpoLista() && matchT("CLOSE_PARENTHESIS", ")");
        }
        return false;
    }

    // ================= CORPO DA LISTA =================
    // corpoLista -> valor entradaLista | ε
    private boolean corpoLista(){
        if (token.tipo.equals("CLOSE_BRACKETS") || token.tipo.equals("CLOSE_PARENTHESIS")){
            return true;
        }
        if (valor()){
            return entradaLista();
        }
        return true;
    }

    // ================= ENTRADA LISTA =================
    // entradaLista -> COMMA valor entradaLista | ε
    private boolean entradaLista(){
        if (matchT("COMMA", ",")){
            return valor() && entradaLista();
        }
        return true;
    }

    // ================= LISTA =================
    // lista -> [ corpoLista ]
    private boolean lista(){
        if (matchT("OPEN_BRACKETS", "[")){
            boolean result = corpoLista() && matchT("CLOSE_BRACKETS", "]");
            return result;
        }
        return false;
    }

    // ================= CONDIÇÃO IF =================
    // cmdIf -> IF valor THEN INDENT bloco DEDENT cmdElse
    private boolean cmdIf(){
        ast.addRuleNode("cmdIf");
        if (matchT("OP_IF", "if") &&                        
            valor() &&                                                   
            matchT("OPEN_BRACES", "{") &&                   
            bloco() &&                                                       
            matchT("CLOSE_BRACES", "}") &&                  
            cmdElse()
            )
        {
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    // ================= CONDIÇÃO ELSE =================
    // cmdElse -> ELSE INDENT bloco DEDENT | ε
    private boolean cmdElse(){
        if (matchT("OP_ELSE", "else") &&                    
            matchT("OPEN_BRACES", "{") &&                  
            bloco() &&                                                       
            matchT("CLOSE_BRACES", "}")                    
            )
        {
            return true;
        }
        return true;
    }

    // ================= LAÇO WHILE =================
    // cmdWhile -> WHILE valor INDENT bloco DEDENT
    private boolean cmdWhile(){
        ast.addRuleNode("cmdWhile");
        if (matchT("OP_WHILE", "while") &&                  
            valor() &&                                                    
            matchT("OPEN_BRACES", "{") &&                   
            bloco() &&                                                       
            matchT("CLOSE_BRACES", "}")                     
            )
        {
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    // ================= LOOP FOR =================
    // cmdFor -> FOR ( variavelFor ; expressaoRelacional ; expressaoAritmetica ) { bloco }
    private boolean cmdFor(){
        ast.addRuleNode("cmdFor");
        if (matchT("OP_FOR", "for") &&                                                              
            matchT("OPEN_PARENTHESIS", "(") &&
            variavelFor() &&
            matchT("SEMICOLON", ";") &&
            expressaoRelacional() &&
            matchT("SEMICOLON", ";") &&
            expressaoAritmetica() &&
            matchT("CLOSE_PARENTHESIS", ")") &&
            matchT("OPEN_BRACES", "{") &&
            bloco() &&
            matchT("CLOSE_BRACES", "}"))
        {
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    // ================= VARIÁVEL FOR =================
    // variavelFor -> tipo ID complemento | ID complemento
    private boolean variavelFor(){
        // Tenta declaração com tipo
        if (tipo()){
            if (id() && complemento()){
                lastType = null;  // Reseta o tipo após usar
                return true;
            }
            lastType = null;  // Reseta o tipo em caso de erro
            return false;
        }
        // Tenta apenas atribuição
        if (id() && complemento()){
            return true;
        }
        return false;
    }

    // ================= FUNÇÃO =================
    // cmdDefFunc -> FUNCTION ID ( listaParametros ) { bloco }
    private boolean cmdDefFunc(){
        ast.addRuleNode("cmdDefFunc");
        if (matchT("OP_FUNCTION", "void ") &&                                                                       
            id() &&
            matchT("OPEN_PARENTHESIS", "(") &&
            listaParametros() &&
            matchT("CLOSE_PARENTHESIS", ")") &&
            matchT("OPEN_BRACES", "{") &&
            bloco() &&
            matchT("CLOSE_BRACES", "}"))
        {
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    // ================= LISTA DE PARÂMETROS =================
    // listaParametros -> ID entradaListaParam | ε
    private boolean listaParametros(){
        if (id()){
            return entradaListaParam();
        }
        return true;
    }

    // ================= ENTRADA LISTA PARÂMETROS =================
    // entradaListaParam -> COMMA ID entradaListaParam | ε
    private boolean entradaListaParam(){
        if (matchT("COMMA", ",")){
            return id() && entradaListaParam();
        }
        return true;
    }

    // ================= RETORNO =================
    // cmdReturn -> RETURN valorRetorno
    private boolean cmdReturn(){
        ast.addRuleNode("cmdReturn");
        if (matchT("OP_RETURN", "return")){
            valorRetorno();
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    // ================= VALOR RETORNO =================
    // valorRetorno -> valor | ε
    private boolean valorRetorno(){
        if (token.tipo.equals("EOF") || token.tipo.equals("CLOSE_BRACES") || 
            token.tipo.equals("OP_CONTINUE") || token.tipo.equals("OP_BREAK")){
            return true;
        }
        return valor();
    }

    // ================= PRINT =================
    // cmdPrint -> PRINT ( corpoLista )
    private boolean cmdPrint(){
        ast.addRuleNode("cmdPrint");
        if (matchT("OP_PRINT", "printf") && 
            matchT("OPEN_PARENTHESIS", "(") &&
            corpoLista() &&
            matchT("CLOSE_PARENTHESIS", ")") &&
            matchT("SEMICOLON", ";")){
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }

    // ================= INPUT =================
    // cmdInput -> INPUT ( corpoLista )
    private boolean cmdInput(){
        ast.addRuleNode("cmdInput");
        if (matchT("OP_INPUT", "scanf") && 
            matchT("OPEN_PARENTHESIS", "(") &&
            corpoLista() &&
            matchT("CLOSE_PARENTHESIS", ")") &&
            matchT("SEMICOLON", ";")){
            ast.endRuleNode();
            return true;
        }
        ast.endRuleNode();
        return false;
    }


    // ================= TIPO DA VARIÁVEL =================
    private boolean tipo(){
        if (matchT("INT_TYPE", "int ")){
            ast.addTerminalNode("int");
            lastType = "int";
            return true;
        }
        if (matchT("FLOAT_TYPE", "float ")){
            ast.addTerminalNode("float");
            lastType = "float";
            return true;
        }
        if (matchT("BOOL_TYPE", "bool ")){
            ast.addTerminalNode("bool");
            lastType = "bool";
            return true;
        }
        if (matchT("STR_TYPE", "char* ")){
            ast.addTerminalNode("char*");
            lastType = "string";
            return true;
        }
        return false;
    }

    // ================= ID =================
    private boolean id(){
        if (matchT("ID", token.lexema)){
            return true;
        }
        return false;
    }

    // ================= ESCRITA NO ARQUIVO EM .c =================
    private void traduz(String code){
        //System.out.print(code); DEBUG
        write(code);
    }

    private void write(String code){
        if(this.writer == null) return;
        try{
            this.writer.write(code);
        } catch (IOException e){
            System.out.println("Erro ao escrever no arquivo: " + e.getMessage());
        }
    }

    private void writeLine(String code){
        write(code);
    }

    private void closeWriter(){
        if(this.writer == null) return;
        try{
            this.writer.close();
        } catch (IOException e){
            System.out.println("Erro ao fechar o arquivo: " + e.getMessage());
        }
    }

    // ================= LEITURA DE TOKENS =================

    // Pegar Lexema do token
    private boolean matchL(String palavra){
        if(token.lexema.equals(palavra)){
            token = getNextToken();
            return true;
        }
        return false;
    }

    // E colocar algo no arquivo .c 
    private boolean matchL(String palavra, String newcode){
        if(token.lexema.equals(palavra)){
            traduz(newcode);
            token = getNextToken();
            return true;
        }
        return false;
    }

    // Pegar tipo do token
    private boolean matchT(String palavra){
        if (token != null && token.tipo.equals(palavra)){
            if(token.linha > currentLine){
                currentLine = token.linha;
            }
            token = getNextToken();
            return true;
        }
        return false;
    }

    // E colocar algo no arquivo .c
    private boolean matchT(String palavra, String newcode){
        if(token != null && token.tipo.equals(palavra)){
            if(token.linha > currentLine){
                currentLine = token.linha;
            }
            traduz(newcode);
            token = getNextToken();
            return true;
        }
        return false;
    }
}
