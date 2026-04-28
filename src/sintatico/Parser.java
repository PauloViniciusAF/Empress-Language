package sintatico;

import java.util.List;

import lexer.Token;

public class Parser {
    List<Token> tokens;
    Token token;
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    // ================= FUNÇÃO PRINCIPAL =================
    public void main(){
        System.out.println("#include <stdio.h>\n#include <stdlib.h>");
        System.out.println("\n");
        System.out.println("int main(){\n");
        token = getNextToken();
        if(file()){
            if(token.tipo == "EOF"){
                System.out.println("\nreturn 0;\n}");
                return;
            }
            else{
                erro();
            }
        }
        erro();
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
        if (atribuicao()){
            comando_if();
            return true;
        }
        return false;
    }

    private boolean comando_if(){
        if (matchT("OP_IF", "if") &&                        // if
            condicao() &&                                                    // alguma coisa
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
        if (id() && operadorAtibuicao() && expressao()){    
            System.out.println(";");
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
        System.out.print("(");
        if(id() && operador() && num()){
            System.out.print(")");
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
