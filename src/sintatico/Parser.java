package sintatico;

import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import lexer.Token;

public class Parser {
    List<Token> tokens;
    Token token;
    private BufferedWriter writer;
    private String outFilePath;

    public Parser(List<Token> tokens, String inputFilePath) {
        this.tokens = tokens;
        try{
            String outName = inputFilePath;
            if(outName.endsWith(".emp")){
                outName = outName.substring(0, outName.length()-4) + ".c";
            } else {
                outName = outName + ".c";
            }
            this.outFilePath = outName;
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
        System.out.print(headers + mainStart);
        write(headers);
        write(mainStart);
        token = getNextToken();
        if(file()){
            if(token != null && "EOF".equals(token.tipo)){
                String ret = "\nreturn 0;\n}\n";
                System.out.print(ret);
                writeLine(ret);
                closeWriter();
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
        if(tokens.size() > 0){
            return tokens.remove(0);
        }
        else{
            return null;
        }
    }
    private void erro(){
        System.out.println("token invalido: "+token.lexema);
    }

    // ================================== GRAMÁTICA ==================================

    // ================= INICIA ARQUIVO =================
    private boolean file(){
        if (bloco() && token != null && token.tipo.equals("EOF")){
            return true;
        }
        return false;
    }

    // ================= BLOCO DE CÓDIGO =================
    private boolean bloco(){
        if (comando()){
            return bloco(); // tenta executar mais comandos recursivamente
        }
        // Se não conseguiu nenhum comando, retorna true apenas se bloco está vazio (próximo token é válido)
        // Se houver tokens restantes que não são comandos válidos, isso será capturado em file()
        return true; 
    }

    // ================= COMANDOS =================
    private boolean comando(){
        if (atribuicao() ||
            comando_if() ||  
            laco_while() || 
            comando_for() ||
            comando_funcao() || 
            comando_retorno() ||
            comando_print() || 
            comando_input() || 
            comando_continue() ||
            comando_break()){
            return true;
        }
        return false;
    }

    // ================= ATRIBUIÇÃO DE VALOR =================
    private boolean atribuicao(){
        if (tipo() && 
            id() && 
            operadorAtibuicao() && 
            expressao() && 
            matchL(";", ";")){
            return true;
        }
        return false;
    }

    // ================= OPERADOR DE ATRIBUIÇÃO =================
    private boolean operadorAtibuicao(){
        if (matchT("ASSIGN", "=") ||
        matchT("PLUS_ASSIGN", "+=") ||
        matchT("TIMES_ASSIGN", "*=") ||
        matchT("MINUS_ASSIGN", "-=") ||
        matchT("POW_ASSIGN", "^=")){
            return true;
        }
        return false;
    }

    // ================= ACESSO OPERADORES =================
    private boolean acessoOp(){
        if (matchT("OPEN_BRACKETS", "[") &&
        expressao() &&
        matchT("OPEN_BRACKETS", "]")){
            return true;
        }
        return false;
    }

     // ================= EXPRESSÃO =================
    private boolean expressao(){
        if (disjuncao()){
            return true;
        }
        return false;
    }

     // ================= DISJUNÇÃO =================
    private boolean disjuncao(){   
        if (!conjuncao()){
            return false;
        }
        while (matchT("OR", "||")){
            if (!conjuncao()){
                return false;
            }
        }
        return true;
    }

     // ================= CONJUNÇÃO =================
    private boolean conjunção(){   
        if (!comparacao()){
            return false;
        }
        while (matchT("AND", "&&")){
            if (!comparacao()){
                return false;
            }
        }
        return true;
    }

     // ================= COMPARAÇÃO =================
    private boolean comparacao(){   
        if (!aritmetima()){
            return false;
        }
        while (op_comparacao()){
            if (!aritmetica()){
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
            matchT("LESS_EQUAL", ">=")
        ){
            return true;
        }
        return false;
    }



     // ================= ARITMETICA =================
    private boolean aritmetica(){   
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
    private boolean fator(){   
        if (!base()){
            return false;
        }
        while (matchT("POW", "^")){
            if (!base()){
                return false;
            }
        }
        return true;
    }

    // ================= BASE =================
    private boolean base(){
        if (primario() && 
            chamadaOp()){
            return true;
        }
        return false;
    }

    // ================= CHAMADA OPERAÇÃO =================
    private boolean chamadaOperacao(){
        if (matchT("OPEN_PARENTHESIS", "(") && 
            corpoLista() &&
            matchT("CLOSE_PARENTHESIS", ")")){
            return true;
        }
        return false;
    }

    // ================= PRIMARIO (TIPOS) =================
    private boolean primario(){
        if (matchT("INT_TYPE", "int ") ||
            matchT("FLOAT_TYPE", "float ") ||
            matchT("BOOL_TYPE", "boolean ") ||
            matchT("STR_TYPE", "string ")) || 
            matchT("ID", token.tipo) ||     
            lista() || 
            (matchT("OPEN_PARENTHESIS", "(") &&
            expressao() && 
            matchT("CLOSE_PARENTHESIS", ")"))
            {            
            return true;
        }
        return false;
    }


    // ================= CONDIÇÃO IF =================
    private boolean comando_if(){
        if (matchT("OP_IF", "if") &&                        // if
            condicao() &&                                                    // condicao
            matchT("OPEN_BRACES", "{") &&                   // {
            bloco() &&                                                       // faz isso
            matchT("CLOSE_BRACES", "}")                     // }
            )
        {
            return true;
        }
        return false;
    }

    // ================= LAÇO WHILE =================
    private boolean laco_while(){
        if (matchT("OP_WHILE", "while") &&                  // while
            expressao() &&                                                    // condicao
            matchT("OPEN_BRACES", "{") &&                   // {
            bloco() &&                                                       // faz isso
            matchT("CLOSE_BRACES", "}")                     // }
            )
        {
            return true;
        }
        return false;
    }


    // ================= LOOP FOR =================
    private boolean comando_for(){
        if (matchT("OP_FOR", "for") &&                      //for                                                                  
            condicao() &&                                                   //
            matchT("OPEN_BRACES", "{") &&
            bloco() &&
            matchT("CLOSE_BRACES", "}"))
        {
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
    // ================= NUM =================
    private boolean num(){
        if(matchT("INT", token.lexema) || matchT("FLOAT", token.lexema)){
            return true;
        }
        return false;
    }

    // ================= TIPO DA VARIÁVEL =================
    private boolean tipo(){
        if (matchT("INT_TYPE", "int ") ||
            matchT("FLOAT_TYPE", "float ") ||
            matchT("BOOL_TYPE", "boolean ") ||
            matchT("STR_TYPE", "string "))
            {            
            return true;
        }
        return false;
    }

    // ================= STRING =================
    private boolean string(){
        if(matchT("STR", token.lexema)){
            return true;
        }
        return false;
    }    

    // ================= BOOLEAN =================
    private boolean bool(){
        if(matchT("BOOL", token.lexema)){
            return true;
        }
        return false;
    } 

    

    

    private boolean condicao(){
        if(id() && comparacao() && num()){
            return true;
        }
        
        return false;
    }

    

    


    // ================= COLOCA NO ARQUIVO EM .c =================
    // OBS: Após debug, retirar 'System.out.print(code);'
    private void traduz(String code){
        System.out.print(code);
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
        if (token.tipo.equals(palavra)){
            token = getNextToken();
            return true;
        }
        return false;
    }

    // E colocar algo no arquivo .c
    private boolean matchT(String palavra, String newcode){
        if(token.tipo.equals(palavra)){
            traduz(newcode);
            token = getNextToken();
            return true;
        }
        return false;
    }

}
