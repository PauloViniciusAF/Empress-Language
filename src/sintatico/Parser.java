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
        String headers = "#include <stdio.h>\n#include <stdlib.h>\n\n";
        String mainStart = "int main(){\n";
        System.out.print(headers + mainStart);
        write(headers);
        write(mainStart);
        token = getNextToken();
        if(file()){
            if(token != null && "EOF".equals(token.tipo)){
                String ret = "\nreturn 0;\n}";
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

    // ================= INICIA ARQUIVO =================
    private boolean file(){
        if (bloco() && token.tipo == "EOF"){
            return true;
        }
        return false;
    }

    // ================= BLOCO DE CÓDIGO =================
    private boolean bloco(){
        if (comando()){
            return true;
        }
        return false;
    }

    // ================= COMANDOS =================
    private boolean comando(){
        if (comando_if()){
            return true;
        }
        return false;
    }

    private boolean comando_if(){
        if (matchT("OP_IF", "if") &&                        // if
            condicao() &&                                                    // condicao
            matchT("OPEN_BRACKETS", "{") &&                 // {
            bloco() &&                                                       // expressao
            matchT("CLOSE_BRACKETS", "}")                   // }
            )
        {
            return true;
        }
        return false;
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

    // ================= STRING =================
    private boolean string(){
        if(matchT("STR", token.lexema)){
            return true;
        }
        return false;
    }    

    private boolean atribuicao(){
        if (id() && operadorAtibuicao() && expressao() && matchL(";", ";")){
            return true;
        }
        return false;
    }

    private boolean expressao(){
        if (id() || num()){
            return true;
        }
        return false;
    }

    private boolean condicao(){
        if(id() && operador() && num()){
            return true;
        }
        
        return false;
    }
    private boolean operador(){
        if (matchL(">", ">") || 
            matchL("<", "<") ||     
            matchL("==", "==")){
            return true;
        }
        return false;
    }

    private boolean operadorAtibuicao(){
        if (matchT("ASSIGN", "=") ||
        matchT("PLUS_ASSIGN", "+=") ||
        matchT("TIMES_ASSIGN", "*=") ||
        matchT("MINUS_ASSIGN", "-=") ||
        matchT("POW_ASSIGN", "^:=")){
            return true;
        }
        return false;
    }

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

    private boolean matchL(String palavra){
        if(token.lexema.equals(palavra)){
            token = getNextToken();
            return true;
        }
        return false;
    }
    private boolean matchT(String palavra){
        if (token.tipo.equals(palavra)){
            token = getNextToken();
            return true;
        }
        return false;
    }


    private boolean matchL(String palavra, String newcode){
        if(token.lexema.equals(palavra)){
            traduz(newcode);
            token = getNextToken();
            return true;
        }
        return false;
    }


    private boolean matchT(String palavra, String newcode){
        if(token.tipo.equals(palavra)){
            traduz(newcode);
            token = getNextToken();
            return true;
        }
        return false;
    }

    
}
