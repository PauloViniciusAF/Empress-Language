package sintatico;

import java.util.List;

import lexer.Token;

public class Parser {
    List<Token> tokens;
    Token token;
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    public void main(){
        System.out.println("#include <stdio.h>\n#include <stdlib.h>");
        System.out.println("\n");
        System.out.println("int main(){\n");
        token = getNextToken();
        if(ifelse() || lista()){
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

    private boolean ifelse(){
        if(matchL("если", "if") &&          //if
        condicao() &&                                        //expressão
        matchT("OPEN_BRACES", "{") &&       //{
        bloco() &&                                           //statement
        matchT("CLOSE_BRACES", "}") &&      //}
        matchL("иначе", "\nelse") &&        //else
        matchT("OPEN_BRACES", "{") &&       //{
        bloco() &&                                           //statement
        matchT("CLOSE_BRACES", "}"))        //}
        {
            return true; 
        } 
        return false;
    }
    
    private boolean lista(){
    if (id() &&                                             //id
    matchT("ASSIGN", "[] = ") &&           //[]
    matchT("OPEN_BRACKET", "{") &&        //{
    corpoLista() &&                                         //corpoLista
    matchT("CLOSE_BRACKET", "}"))         //}
    {
        return true;
    }
    return false;
    }

    private boolean corpoLista(){
        if(id())
        {
            return true;
        }
        return false;
    }


    private boolean bloco(){
        if (id() && operadorAtibuicao() && num()){
            return true;
            
        }
        
        return false;
    }
    private boolean operadorAtibuicao(){
        if (matchL(":=", "=")){
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

    private boolean id(){
        if (matchT("ID", token.lexema)){
            return true;
        }
        return false;
    }
    private boolean num(){
        if(matchT("INT", token.lexema)){
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
